package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class TagIngredient extends BaseIngredient {

    private Tag<Material> tag;

    public TagIngredient(Tag<Material> tag, Character sign) {
        super(sign);
        this.tag = tag;
    }

    public TagIngredient(Tag<Material> tag) {
        this(tag, null);
    }

    @Override
    public boolean isSimilar(ItemStack item) {
        return this.tag.isTagged(item.getType());
    }

    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(this.tag);
    }
}
