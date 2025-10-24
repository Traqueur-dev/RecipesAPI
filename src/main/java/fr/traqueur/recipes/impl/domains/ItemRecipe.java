package fr.traqueur.recipes.impl.domains;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.api.Util;
import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.api.hook.Hook;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

/**
 * This class represents a recipe for an item
 * @param recipeName The name of the recipe
 * @param group The group of the recipe
 * @param category The category of the recipe
 * @param recipeType The type of the recipe
 * @param result The result of the recipe
 * @param amount The amount of the result
 * @param ingredients The ingredients of the recipe
 * @param pattern The pattern of the recipe
 * @param cookingTime The cooking time of the recipe
 * @param experience The experience of the recipe
 * @param priority The priority of the recipe (higher = registered first)
 */
public record ItemRecipe(String recipeName, String group, String category, RecipeType recipeType, String result, int amount, Ingredient[] ingredients,
                         String[] pattern, int cookingTime, float experience, int priority) {

    /**
     * Convert the recipe to a bukkit recipe
     * @param key The key of the recipe
     * @param result The result of the recipe
     * @return The bukkit recipe
     */
    public Recipe toBukkitRecipe(NamespacedKey key, ItemStack result) {
        return switch (this.recipeType) {
            case CRAFTING_SHAPED -> {
                var shapedRecipe = new ShapedRecipe(key, result);
                shapedRecipe.shape(pattern);
                for (Ingredient ingredient : ingredients) {
                    shapedRecipe.setIngredient(ingredient.sign(), ingredient.choice());
                }
                if (!group.isEmpty()) {
                    shapedRecipe.setGroup(group);
                }
                if (!category.isEmpty()) {
                    shapedRecipe.setCategory(CraftingBookCategory.valueOf(category.toUpperCase()));
                }
                yield shapedRecipe;
            }
            case CRAFTING_SHAPELESS -> {
                var shapelessRecipe = new ShapelessRecipe(key, result);
                for (Ingredient ingredient : ingredients) {
                    shapelessRecipe.addIngredient(ingredient.choice());
                }
                if (!group.isEmpty()) {
                    shapelessRecipe.setGroup(group);
                }
                if (!category.isEmpty()) {
                    shapelessRecipe.setCategory(CraftingBookCategory.valueOf(category.toUpperCase()));
                }
                yield shapelessRecipe;
            }
            case BLASTING -> {
                var blastingRecipe = new BlastingRecipe(key, result, ingredients[0].choice(), experience, cookingTime);
                if (!group.isEmpty()) {
                    blastingRecipe.setGroup(group);
                }
                if (!category.isEmpty()) {
                    blastingRecipe.setCategory(CookingBookCategory.valueOf(category.toUpperCase()));
                }
                yield blastingRecipe;
            }
            case CAMPFIRE_COOKING -> {
                var campfireRecipe = new CampfireRecipe(key, result, ingredients[0].choice(), experience, cookingTime);
                if (!group.isEmpty()) {
                    campfireRecipe.setGroup(group);
                }
                if (!category.isEmpty()) {
                    campfireRecipe.setCategory(CookingBookCategory.valueOf(category.toUpperCase()));
                }
                yield campfireRecipe;
            }
            case SMOKING -> {
                var smokingRecipe = new SmokingRecipe(key, result, ingredients[0].choice(), experience, cookingTime);
                if (!group.isEmpty()) {
                    smokingRecipe.setGroup(group);
                }
                if (!category.isEmpty()) {
                    smokingRecipe.setCategory(CookingBookCategory.valueOf(category.toUpperCase()));
                }
                yield smokingRecipe;
            }
            case STONE_CUTTING -> {
                var stonecuttingRecipe = new StonecuttingRecipe(key, result, ingredients[0].choice());
                if (!group.isEmpty()) {
                    stonecuttingRecipe.setGroup(group);
                }
                yield stonecuttingRecipe;
            }
            case SMELTING -> {
                var furnaceRecipe = new FurnaceRecipe(key, result, ingredients[0].choice(), experience, cookingTime);
                if (!group.isEmpty()) {
                    furnaceRecipe.setGroup(group);
                }
                if (!category.isEmpty()) {
                    furnaceRecipe.setCategory(CookingBookCategory.valueOf(category.toUpperCase()));
                }
                yield furnaceRecipe;
            }
            case SMITHING_TRANSFORM -> new SmithingTransformRecipe(key, result, ingredients[0].choice(), ingredients[1].choice(), ingredients[2].choice());
        };
    }

    /**
     * Convert the recipe to a bukkit recipe
     * @return The bukkit recipe
     */
    public Recipe toBukkitRecipe() {
        ItemStack result = this.toBukkitItemStack(null);
        NamespacedKey key = this.getKey();
        return this.toBukkitRecipe(key, result);
    }

    public ItemStack toBukkitItemStack(Player player) {
        ItemStack result;
        String[] resultParts = this.result.split(":");
        if(resultParts.length == 1) {
            result = new ItemStack(Util.getMaterial(resultParts[0]));
        } else {
            result = switch (resultParts[0]) {
                case "material" -> new ItemStack(Util.getMaterial(resultParts[1]));
                case "item" -> new ItemStack(Util.getMaterial(resultParts[1]));
                case "base64" -> Util.getItemStack(resultParts[1]);
                default -> Hook.HOOKS.stream()
                        .filter(Hook::isEnable)
                        .filter(hook -> hook.getPluginName().equalsIgnoreCase(resultParts[0]))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("The result " + this.result + " isn't valid."))
                        .getItemStack(player, resultParts[1]);
            };
        }
        result.setAmount(this.amount());
        return result;
    }

    /**
     * Get the key of the recipe
     * @return The key of the recipe
     */
    public NamespacedKey getKey() {
        return this.recipeType().getNamespacedKey(recipeName);
    }
}
