package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class ItemStackIngredient extends BaseIngredient {

    protected final ItemStack item;

    public ItemStackIngredient(ItemStack item, Character sign) {
        super(sign);
        this.item = item;
    }

    public ItemStackIngredient(ItemStack item) {
        this(item, null);
    }

    @Override
    public boolean isSimilar(ItemStack item) {

        return item.getType() == this.item.getType()
                && item.getAmount() >= this.item.getAmount()
                && item.hasItemMeta() == this.item.hasItemMeta()
                && (!item.hasItemMeta() || similarMeta(item.getItemMeta(), this.item.getItemMeta()));
    }

    private boolean similarMeta(ItemMeta sourceMeta, ItemMeta ingredientMeta) {
        for (NamespacedKey key : sourceMeta.getPersistentDataContainer().getKeys()) {
            if (!ingredientMeta.getPersistentDataContainer().has(key)) {
                return false;
            }
        }

        boolean lore = sourceMeta.hasLore() == ingredientMeta.hasLore() && (sourceMeta.hasLore()
                && Objects.equals(sourceMeta.getLore(), ingredientMeta.getLore()));

        boolean customData = sourceMeta.hasCustomModelData() == ingredientMeta.hasCustomModelData()
                && (sourceMeta.hasCustomModelData()
                    && sourceMeta.getCustomModelData() == ingredientMeta.getCustomModelData());

        return lore && customData;
    }

    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(this.item.getType());
    }
}
