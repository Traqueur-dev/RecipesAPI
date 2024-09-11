package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class MaterialIngredient extends BaseIngredient {

    private final Material material;

    public MaterialIngredient(Material material, Character sign) {
        super(sign);
        this.material = material;
    }

    public MaterialIngredient(Material material) {
        this(material, null);
    }

    @Override
    public boolean isSimilar(ItemStack item) {
        return item.getType() == this.material;
    }

    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(this.material);
    }
}
