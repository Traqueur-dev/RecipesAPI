package fr.traqueur.recipes.api.domains;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

/**
 * Base class for ingredients.
 */
public abstract class Ingredient {

    /**
     * The sign of the ingredient.
     */
    private final Character sign;

    /**
     * Constructor.
     * @param sign The sign of the ingredient.
     */
    public Ingredient(Character sign) {
        this.sign = sign;
    }

    /**
     * Get the sign of the ingredient.
     * @return The sign of the ingredient.
     */
    public Character sign() {
        return this.sign;
    }


    /**
     * Check if the item is similar to the ingredient.
     * @param item The item to check.
     * @return true if the item is similar to the ingredient, false otherwise.
     */
    public abstract boolean isSimilar(ItemStack item);

    /**
     * Get the choice of the ingredient.
     * @return The choice of the ingredient.
     */
    public abstract RecipeChoice choice();
}
