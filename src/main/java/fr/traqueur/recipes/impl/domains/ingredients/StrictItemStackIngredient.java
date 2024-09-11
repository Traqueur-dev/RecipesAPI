package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * This class represents an ingredient that is an item stack with strict comparison
 */
public class StrictItemStackIngredient extends ItemStackIngredient {

    /**
     * Create a new StrictItemStackIngredient
     * @param item The item of the ingredient
     * @param sign The sign of the ingredient
     */
    public StrictItemStackIngredient(ItemStack item, Character sign) {
        super(item, sign);
    }

    /**
     * Create a new StrictItemStackIngredient
     * @param item The item of the ingredient
     */
    public StrictItemStackIngredient(ItemStack item) {
        super(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(ItemStack item) {
        return item.isSimilar(this.item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.ExactChoice(this.item);
    }
}
