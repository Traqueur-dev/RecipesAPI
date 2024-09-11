package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class StrictItemStackIngredient extends ItemStackIngredient {


    public StrictItemStackIngredient(ItemStack item, Character sign) {
        super(item, sign);
    }

    public StrictItemStackIngredient(ItemStack item) {
        super(item);
    }

    @Override
    public boolean isSimilar(ItemStack item) {
        return item.isSimilar(this.item);
    }

    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.ExactChoice(this.item);
    }
}
