package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.Ingredient;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

/**
 * This class represents an ingredient that is an item stack
 */
public class ItemStackIngredient extends Ingredient {

    /**
     * The item of the ingredient
     */
    protected final ItemStack item;

    /**
     * Create a new ItemStackIngredient
     * @param item The item of the ingredient
     * @param sign The sign of the ingredient
     */
    public ItemStackIngredient(ItemStack item, Character sign) {
        super(sign);
        this.item = item;
    }

    /**
     * Create a new ItemStackIngredient
     * @param item The item of the ingredient
     */
    public ItemStackIngredient(ItemStack item) {
        this(item, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(ItemStack item) {

        return item.getType() == this.item.getType()
                && item.getAmount() >= this.item.getAmount()
                && item.hasItemMeta() == this.item.hasItemMeta()
                && (!item.hasItemMeta() || similarMeta(item.getItemMeta(), this.item.getItemMeta()));
    }

    /**
     * Check if the meta of the two items are similar
     * @param sourceMeta The source meta
     * @param ingredientMeta The ingredient meta
     * @return True if the meta of the two items are similar
     */
    private boolean similarMeta(ItemMeta sourceMeta, ItemMeta ingredientMeta) {
        for (NamespacedKey key : sourceMeta.getPersistentDataContainer().getKeys()) {
            if (!ingredientMeta.getPersistentDataContainer().has(key)) {
                System.out.println("Key " + key + " not found in ingredient meta");
                return false;
            }
        }

        boolean lore = sourceMeta.hasLore() == ingredientMeta.hasLore() && (!sourceMeta.hasLore()
                || Objects.equals(sourceMeta.getLore(), ingredientMeta.getLore()));

        boolean customData = sourceMeta.hasCustomModelData() == ingredientMeta.hasCustomModelData()
                && (!sourceMeta.hasCustomModelData()
                    || sourceMeta.getCustomModelData() == ingredientMeta.getCustomModelData());

        return lore && customData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(this.item.getType());
    }

    @Override
    public String toString() {
        return "ItemStackIngredient{" +
                "item=" + item +
                '}';
    }
}
