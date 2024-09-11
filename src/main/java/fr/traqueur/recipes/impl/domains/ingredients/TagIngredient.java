package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

/**
 * This class represents an ingredient that is a tag
 */
public class TagIngredient extends BaseIngredient {

    /**
     * The tag of the ingredient
     */
    private final Tag<Material> tag;

    /**
     * Create a new TagIngredient
     * @param tag The tag of the ingredient
     * @param sign The sign of the ingredient
     */
    public TagIngredient(Tag<Material> tag, Character sign) {
        super(sign);
        this.tag = tag;
    }

    /**
     * Create a new TagIngredient
     * @param tag The tag of the ingredient
     */
    public TagIngredient(Tag<Material> tag) {
        this(tag, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(ItemStack item) {
        return this.tag.isTagged(item.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(this.tag);
    }
}
