package fr.traqueur.recipes.impl;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.api.RecipesAPI;
import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is used to listen to events that are related to the api.
 */
public class PrepareCraftListener implements Listener {

    /**
     * The API instance.
     */
    private final RecipesAPI api;

    /**
     * Creates a new PrepareCraftListener instance.
     * @param api the API instance
     */
    public PrepareCraftListener(RecipesAPI api) {
        this.api = api;
    }

    /**
     * Get the recipes for an item.
     * @param item the item
     * @param recipeType the recipe type
     */
    private <T extends Recipe> List<T> getRecipeFor(ItemStack item, Class<T> recipeType) {
        List<T> recipes = new ArrayList<>();
        for (Recipe recipe : api.getPlugin().getServer().getRecipesFor(item)) {
            if (recipeType.isInstance(recipe)) {
                recipes.add(recipeType.cast(recipe));
            }
        }
        return recipes;
    }


    /**
     * This method is called when a block is smelted.
     * @param event the event
     */
    @EventHandler
    public void onSmelt(BlockCookEvent event) {
        if(event.isCancelled()) {
            return;
        }
        ItemStack item = event.getSource();
        if (item == null || item.getType() == Material.AIR) return;
        ItemStack result = event.getResult();
        var recipes = getRecipeFor(result, FurnaceRecipe.class);

        List<ItemRecipe> itemRecipes = api.getRecipes().stream()
                .filter(itemRecipe ->  RecipeType.smeltingRecipes().contains(itemRecipe.recipeType()))
                .toList();

        for (ItemRecipe itemRecipe : itemRecipes) {
            recipes.stream()
                    .filter(recipe -> recipe.getKey().equals(itemRecipe.getKey()))
                    .findFirst()
                    .ifPresent(recipe -> {
                if(!isSimilar(item, itemRecipe.ingredients()[0])) {
                    this.api.debug("The smelting recipe %s is not good.", itemRecipe.getKey());
                    event.setCancelled(true);
                } else {
                    this.api.debug("The smelting recipe %s is good.", itemRecipe.getKey());
                    event.setResult(itemRecipe.toBukkitItemStack(null));
                }
            });
        }
    }

    /**
     * This method is called when a smithing transformation is prepared.
     * @param event the event
     */
    @EventHandler
    public void onSmithingTransform(PrepareSmithingEvent event) {
        if(event.getInventory().getRecipe() == null) {
            return;
        }
        SmithingRecipe recipe =  (SmithingRecipe) event.getInventory().getRecipe();

        ItemStack item = event.getResult();
        if (item == null || item.getType() == Material.AIR) return;

        ItemStack template = event.getInventory().getItem(0);
        ItemStack base = event.getInventory().getItem(1);
        ItemStack addition = event.getInventory().getItem(2);

        if(recipe instanceof SmithingTrimRecipe) {
            return;
        }


        List<ItemRecipe> itemRecipes = api.getRecipes().stream()
                .filter(itemRecipe -> itemRecipe.recipeType() == RecipeType.SMITHING_TRANSFORM)
                .toList();
        for (ItemRecipe itemRecipe : itemRecipes) {
            if (!itemRecipe.getKey()
                    .equals(recipe.getKey()))
                continue;
            this.api.debug("The recipe %s is a smithing recipe.", itemRecipe.getKey());
            Ingredient templateIngredient = itemRecipe.ingredients()[0];
            Ingredient baseIngredient = itemRecipe.ingredients()[1];
            Ingredient additionIngredient = itemRecipe.ingredients()[2];

            boolean isSimilar = isSimilar(template, templateIngredient)
                    && isSimilar(base, baseIngredient)
                    && isSimilar(addition, additionIngredient);

            if(!isSimilar) {
                this.api.debug("The smithing recipe %s is not good.", itemRecipe.getKey());
                event.setResult(new ItemStack(Material.AIR));
                return;
            }
            this.api.debug("The smithing recipe %s is good.", itemRecipe.getKey());
            event.setResult(itemRecipe.toBukkitItemStack((Player) event.getViewers().getFirst()));
        }
    }

    /**
     * Check if an item is similar to an ingredient.
     * @param item the item
     * @param itemIngredient the ingredient
     * @return true if the item is similar to the ingredient, false otherwise
     */
    private boolean isSimilar(ItemStack item, Ingredient itemIngredient) {
        return itemIngredient.isSimilar(item);
    }

    /**
     * This method is called when an item is prepared to be crafted.
     * @param event the event
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) return;

        var itemRecipes = api.getRecipes().stream()
                .filter(itemRecipe -> itemRecipe.recipeType() == RecipeType.CRAFTING_SHAPED || itemRecipe.recipeType() == RecipeType.CRAFTING_SHAPELESS)
                .toList();

        for (ItemRecipe itemRecipe : itemRecipes) {
            if(recipe instanceof ShapedRecipe shapedRecipe && itemRecipe.recipeType() == RecipeType.CRAFTING_SHAPED) {
                if (!shapedRecipe.getKey().equals(itemRecipe.getKey())) continue;
                this.api.debug("The recipe %s is a shaped recipe.", itemRecipe.getKey());
                this.checkGoodShapedRecipe((Player) event.getViewers().getFirst(), itemRecipe, event);
            }

            if(recipe instanceof ShapelessRecipe shapelessRecipe && itemRecipe.recipeType() == RecipeType.CRAFTING_SHAPELESS) {
                if(!shapelessRecipe.getKey().equals(itemRecipe.getKey())) continue;
                this.api.debug("The recipe %s is a shapeless recipe.", itemRecipe.getKey());
                this.checkGoodShapelessRecipe((Player) event.getViewers().getFirst(), itemRecipe, event);
            }
        }
    }

    /**
     * Check if the recipe is good for a shaped recipe.
     * @param itemRecipe the item recipe
     * @param event the event
     */
    private void checkGoodShapedRecipe(Player player, ItemRecipe itemRecipe, PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        String[] pattern = Arrays.stream(itemRecipe.pattern()).map(s -> s.split("")).flatMap(Arrays::stream).toArray(String[]::new);

        for (int i = 0; i < matrix.length && i < pattern.length; i++) {
            ItemStack stack = matrix[i];
            char sign = pattern[i].charAt(0);

            // Si le pattern indique un espace (air), vérifier que l'item est null ou AIR
            if (sign == ' ') {
                if (stack != null && stack.getType() != Material.AIR) {
                    this.api.debug("The shaped recipe %s is not good - expected air at position %d.", itemRecipe.getKey(), i);
                    event.getInventory().setResult(new ItemStack(Material.AIR));
                    return;
                }
                continue;
            }

            // Si l'item est null ou AIR mais que le pattern attend un ingrédient
            if (stack == null || stack.getType() == Material.AIR) {
                this.api.debug("The shaped recipe %s is not good - missing ingredient at position %d.", itemRecipe.getKey(), i);
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }

            AtomicBoolean isSimilar = new AtomicBoolean(false);
            Arrays.stream(itemRecipe.ingredients()).filter(ingredient -> ingredient.sign() == sign).findFirst().ifPresent(ingredient -> {
                isSimilar.set(ingredient.isSimilar(stack));
            });
            if(!isSimilar.get()) {
                this.api.debug("The shaped recipe %s is not good - ingredient mismatch at position %d.", itemRecipe.getKey(), i);
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }

        this.api.debug("The shaped recipe %s is good.", itemRecipe.getKey());
        event.getInventory().setResult(itemRecipe.toBukkitItemStack(player));
    }

    /**
     * Check if the recipe is good for a shapeless recipe.
     * @param itemRecipe the item recipe
     * @param event the event
     */
    private void checkGoodShapelessRecipe(Player player, ItemRecipe itemRecipe, PrepareItemCraftEvent event) {
        List<ItemStack> matrix = new ArrayList<>(Arrays.stream(event.getInventory().getMatrix()).filter(Objects::nonNull).filter(it -> it.getType() != Material.AIR).toList());
        Ingredient[] itemIngredients = itemRecipe.ingredients();

        if (matrix.size() != itemIngredients.length) {
            this.api.debug("The shapeless recipe %s is not good - wrong number of items.", itemRecipe.getKey());
            event.getInventory().setResult(new ItemStack(Material.AIR));
            return;
        }

        for (Ingredient ingredient : itemIngredients) {
            boolean found = false;
            for (int i = 0; i < matrix.size(); i++) {
                ItemStack stack = matrix.get(i);
                if (stack != null && stack.getType() != Material.AIR && ingredient.isSimilar(stack)) {
                    this.api.debug("Ingredient %s found in the matrix.", ingredient.toString());
                    matrix.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                this.api.debug("Ingredient %s not found in the matrix.", ingredient.toString());
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }

        this.api.debug("The shapeless recipe %s is good.", itemRecipe.getKey());
        event.getInventory().setResult(itemRecipe.toBukkitItemStack(player));
    }
}
