package fr.traqueur.recipes.impl;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.api.RecipesAPI;
import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.*;

import java.util.*;
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
                    event.setCancelled(true);
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
            Ingredient templateIngredient = itemRecipe.ingredients()[0];
            Ingredient baseIngredient = itemRecipe.ingredients()[1];
            Ingredient additionIngredient = itemRecipe.ingredients()[2];

            boolean isSimilar = isSimilar(template, templateIngredient)
                    && isSimilar(base, baseIngredient)
                    && isSimilar(addition, additionIngredient);

            if(!isSimilar) {
                event.setResult(new ItemStack(Material.AIR));
                return;
            }
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
    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (recipe == null) return;

        var itemRecipes = api.getRecipes().stream()
                .filter(itemRecipe -> itemRecipe.recipeType() == RecipeType.CRAFTING_SHAPED || itemRecipe.recipeType() == RecipeType.CRAFTING_SHAPELESS)
                .toList();

        for (ItemRecipe itemRecipe : itemRecipes) {
            if(recipe instanceof ShapedRecipe shapedRecipe && itemRecipe.recipeType() == RecipeType.CRAFTING_SHAPED) {
                if (!shapedRecipe.getKey().equals(itemRecipe.getKey())) continue;
                this.checkGoodShapedRecipe(itemRecipe, event);
            }

            if(recipe instanceof ShapelessRecipe shapelessRecipe && itemRecipe.recipeType() == RecipeType.CRAFTING_SHAPELESS) {
                if(!shapelessRecipe.getKey().equals(itemRecipe.getKey())) continue;
                this.checkGoodShapelessRecipe(itemRecipe, event);
            }
        }
    }

    /**
     * Check if the recipe is good for a shaped recipe.
     * @param itemRecipe the item recipe
     * @param event the event
     */
    private void checkGoodShapedRecipe(ItemRecipe itemRecipe, PrepareItemCraftEvent event) {
        AtomicBoolean isSimilar = new AtomicBoolean(true);
        ItemStack[] matrix = event.getInventory().getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            ItemStack stack = matrix[i];
            if (stack == null || stack.getType() == Material.AIR) continue;
            int row = i / 3;
            int column = i % 3;
            String[] pattern = itemRecipe.pattern();
            char sign = pattern[row].replace(" ", "\u0001").split("")[column].toCharArray()[0];
            Arrays.stream(itemRecipe.ingredients()).filter(ingredient -> ingredient.sign() == sign).findFirst().ifPresent(ingredient -> {
                isSimilar.set(ingredient.isSimilar(stack));
            });
            if(!isSimilar.get()) {
                event.getInventory().setResult(new ItemStack(Material.AIR));
                return;
            }
        }
    }

    /**
     * Check if the recipe is good for a shapeless recipe.
     * @param itemRecipe the item recipe
     * @param event the event
     */
    private void checkGoodShapelessRecipe(ItemRecipe itemRecipe, PrepareItemCraftEvent event) {
        List<ItemStack> matrix = Arrays.stream(event.getInventory().getMatrix()).filter(Objects::nonNull).toList();
        Ingredient[] itemIngredients = itemRecipe.ingredients();

        AtomicBoolean isSimilar = new AtomicBoolean(true);
        for (Ingredient ingredient : itemIngredients) {
            boolean found = matrix.stream().anyMatch(stack -> {
                if (stack == null || stack.getType() == Material.AIR) return false;
                return ingredient.isSimilar(stack);
            });
            if (!found) {
                isSimilar.set(false);
                break;
            }
        }

        if (!isSimilar.get()) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }
}
