package fr.traqueur.recipes.api.domains;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

/**
 * Represents an ingredient.
 */
public interface Ingredient {

    /**
     * Check if the item is similar to the ingredient.
     * @param item The item to check.
     * @return true if the item is similar to the ingredient, false otherwise.
     */
    boolean isSimilar(ItemStack item);

    /**
     * Get the choice of the ingredient.
     * @return The choice of the ingredient.
     */
    RecipeChoice choice();

    /**
     * Get the sign of the ingredient.
     * @return The sign of the ingredient.
     */
    Character sign();

}
