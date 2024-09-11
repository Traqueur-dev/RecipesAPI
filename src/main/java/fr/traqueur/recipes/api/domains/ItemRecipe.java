package fr.traqueur.recipes.api.domains;

import fr.traqueur.recipes.api.RecipeType;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;

public record ItemRecipe(String recipeName, String group, String category, RecipeType recipeType, ItemStack result, int amount, Ingredient[] ingredients,
                         String[] pattern, int cookingTime, float experience) {

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

    public Recipe toBukkitRecipe() {
        ItemStack result = new ItemStack(this.result());
        result.setAmount(this.amount());
        NamespacedKey key = this.recipeType().getNamespacedKey(recipeName);
        return this.toBukkitRecipe(key, result);
    }


    public record Ingredient(RecipeChoice choice, String ingredientName, char sign) { }
}
