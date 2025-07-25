package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.Ingredient;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

/**
 * A material ingredient
 */
public class MaterialIngredient extends Ingredient {

    /**
     * The material of the ingredient
     */
    private final Material material;

    /**
     * Create a new MaterialIngredient
     * @param material The material of the ingredient
     * @param sign The sign of the ingredient
     */
    public MaterialIngredient(Material material, Character sign) {
        super(sign);
        this.material = material;
    }

    /**
     * Create a new MaterialIngredient
     * @param material The material of the ingredient
     */
    public MaterialIngredient(Material material) {
        this(material, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(ItemStack item) {
        return item.getType() == this.material;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(this.material);
    }

    @Override
    public String toString() {
        return this.material.toString();
    }
}
