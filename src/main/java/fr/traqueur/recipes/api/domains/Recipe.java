package fr.traqueur.recipes.api.domains;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import fr.traqueur.recipes.impl.domains.ingredients.ItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.MaterialIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.StrictItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.TagIngredient;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents a recipe.
 */
public interface Recipe {

    /**
     * Set the name of the recipe.
     * @param name The name of the recipe.
     * @return The name of the recipe.
     */
    Recipe setName(String name);

    /**
     * Set the result of the recipe.
     * @param result The result of the recipe.
     * @return The result of the recipe.
     */
    Recipe setResult(ItemStack result);

    /**
     * Set the amount of the result.
     * @param amount The amount of the result.
     * @return The amount of the result.
     */
    Recipe setAmount(int amount);

    /**
     * Set the type of the recipe.
     * @param type The type of the recipe.
     * @return The type of the recipe.
     */
    Recipe setType(RecipeType type);


    /**
     * Add an ingredient to the recipe.
     * @param ingredients The ingredients to add.
     * @return The recipe.
     */
    default Recipe addIngredient(Ingredient... ingredients) {
        for (Ingredient ingredient : ingredients) {
            addIngredient(ingredient);
        }
        return this;
    }

    /**
     * Add an ingredient to the recipe.
     * @param tag The tag of the ingredient.
     * @return The recipe.
     */
    default Recipe addIngredient(Tag<Material> tag) {
        return addIngredient(tag, null);
    }

    /**
     * Add an ingredient to the recipe.
     * @param tag The tag of the ingredient.
     * @param sign The sign of the ingredient.
     * @return The recipe.
     */
    default Recipe addIngredient(Tag<Material> tag, Character sign) {
        return addIngredient(new TagIngredient(tag, sign));
    }

    /**
     * Add an ingredient to the recipe.
     * @param item The item of the ingredient.
     * @return The recipe.
     */
    default Recipe addIngredient(ItemStack item) {
        if(this.getType() == RecipeType.CRAFTING_SHAPED) {
            throw new UnsupportedOperationException("You can't add an ingredient without sign to a shaped recipe");
        }
        return addIngredient(item, null);
    }

    /**
     * Add an ingredient to the recipe.
     * @param item The item of the ingredient.
     * @param strict If the ingredient is strict.
     * @return The recipe.
     */
    default Recipe addIngredient(ItemStack item, boolean strict) {
        return addIngredient(item, null, strict);
    }

    /**
     * Add an ingredient to the recipe.
     * @param item The item of the ingredient.
     * @param sign The sign of the ingredient.
     * @return The recipe.
     */
    default Recipe addIngredient(ItemStack item, Character sign) {
        return addIngredient(item, sign, false);
    }

    /**
     * Add an ingredient to the recipe.
     * @param item The item of the ingredient.
     * @param sign The sign of the ingredient.
     * @param strict If the ingredient is strict.
     * @return The recipe.
     */
    default Recipe addIngredient(ItemStack item, Character sign, boolean strict) {
        return addIngredient(strict ? new StrictItemStackIngredient(item, sign)
                : new ItemStackIngredient(item, sign));
    }

    /**
     * Add an ingredient to the recipe.
     * @param material The material of the ingredient.
     * @return The recipe.
     */
    default Recipe addIngredient(Material material) {
        if(this.getType() == RecipeType.CRAFTING_SHAPED) {
            throw new UnsupportedOperationException("You can't add an ingredient without sign to a shaped recipe");
        }
        return addIngredient(material, null);
    }

    /**
     * Add an ingredient to the recipe.
     * @param material The material of the ingredient.
     * @param sign The sign of the ingredient.
     * @return The recipe.
     */
    default Recipe addIngredient(Material material, Character sign) {
        return addIngredient(new MaterialIngredient(material, sign));
    }

    /**
     * Add an ingredient to the recipe.
     * @param ingredient The ingredient to add.
     * @return The recipe.
     */
    Recipe addIngredient(Ingredient ingredient);

    /**
     * Set the group of the recipe.
     * @param group The group of the recipe.
     * @return The group of the recipe.
     */
    Recipe setGroup(String group);

    /**
     * Set the category of the recipe.
     * @param category The category of the recipe.
     * @return The category of the recipe.
     */
    Recipe setCategory(String category);

    /**
     * Set the pattern of the recipe.
     * @param pattern The pattern of the recipe.
     * @return The pattern of the recipe.
     */
    Recipe setPattern(String... pattern);

    /**
     * Set the cooking time of the recipe.
     * @return The cooking time of the recipe.
     * @param cookingTime The cooking time of the recipe.
     */
    Recipe setCookingTime(int cookingTime);

    /**
     * Set the experience of the recipe.
     * @return The experience of the recipe.
     * @param experience The experience of the recipe.
     */
    Recipe setExperience(float experience);

    /**
     * Get the type of the recipe.
     * @return The type of the recipe.
     */
    RecipeType getType();

    /**
     * Build the recipe.
     * @return The recipe.
     */
    ItemRecipe build();

    /**
     * Create a new item recipe.
     * @param ingredientList The list of ingredients.
     * @param type The type of the recipe.
     * @param pattern The pattern of the recipe.
     * @param cookingTime The cooking time of the recipe.
     * @param name The name of the recipe.
     * @param group The group of the recipe.
     * @param category The category of the recipe.
     * @param result The result of the recipe.
     * @param amount The amount of the result.
     * @param experience The experience of the recipe.
     * @return The item recipe.
     */
    default ItemRecipe getItemRecipe(List<Ingredient> ingredientList, RecipeType type, String[] pattern, int cookingTime, String name, String group, String category, String result, int amount, float experience) {
        if (ingredientList.isEmpty()) {
            throw new IllegalArgumentException("Ingredients are not set");
        }

        if (type == RecipeType.CRAFTING_SHAPED && pattern == null) {
            throw new IllegalArgumentException("Pattern is not set");
        }

        if (type == RecipeType.CRAFTING_SHAPED && pattern.length == 0) {
            throw new IllegalArgumentException("Pattern is empty");
        }

        if(RecipeType.smeltingRecipes().contains(type) && cookingTime == 0) {
            throw new IllegalArgumentException("Cooking time is not set");
        }

        return new ItemRecipe(name, group, category, type, result, amount, ingredientList.toArray(new Ingredient[0]), pattern, cookingTime, experience);
    }

}
