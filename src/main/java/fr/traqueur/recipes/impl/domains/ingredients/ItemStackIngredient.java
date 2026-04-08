package fr.traqueur.recipes.impl.domains.ingredients;

import fr.traqueur.recipes.api.domains.Ingredient;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.persistence.PersistentDataType;

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
        if (item == null || this.item == null) {
            return false;
        }

        // Amount is not compared: the quantity requirement is expressed by having multiple
        // ingredient entries, not by the amount stored in each ingredient ItemStack.
        return item.getType() == this.item.getType()
                && item.getAmount() >= 1
                && item.hasItemMeta() == this.item.hasItemMeta()
                && (!item.hasItemMeta() || similarMeta(item.getItemMeta(), this.item.getItemMeta()));
    }

    /**
     * Check if the meta of the two items are similar.
     * Verifies PDC key existence and values, lore, custom model data, and display name.
     * @param sourceMeta The source meta (item in the crafting grid)
     * @param ingredientMeta The ingredient meta (required ingredient)
     * @return True if the source satisfies all ingredient meta requirements
     */
    private boolean similarMeta(ItemMeta sourceMeta, ItemMeta ingredientMeta) {
        // Check PDC: all ingredient keys must exist in source with matching values
        for (NamespacedKey key : ingredientMeta.getPersistentDataContainer().getKeys()) {
            if (!sourceMeta.getPersistentDataContainer().has(key)) {
                return false;
            }
            if (!comparePDCValue(key, sourceMeta, ingredientMeta)) {
                return false;
            }
        }

        // Check display name (only if ingredient has one)
        if (ingredientMeta.hasDisplayName()) {
            if (!sourceMeta.hasDisplayName() || !ingredientMeta.getDisplayName().equals(sourceMeta.getDisplayName())) {
                return false;
            }
        }

        // Check lore (only if ingredient has lore)
        if (ingredientMeta.hasLore()) {
            if (!sourceMeta.hasLore() || !Objects.equals(sourceMeta.getLore(), ingredientMeta.getLore())) {
                return false;
            }
        }

        // Check custom model data (only if ingredient has custom model data)
        if (ingredientMeta.hasCustomModelData()) {
            return sourceMeta.hasCustomModelData() && sourceMeta.getCustomModelData() == ingredientMeta.getCustomModelData();
        }

        return true;
    }

    /**
     * Compare the value of a PDC key between source and ingredient metas.
     * Tries common primitive types; falls back to existence-only check for complex types.
     * @param key the PDC key to compare
     * @param sourceMeta the source item meta
     * @param ingredientMeta the ingredient item meta
     * @return true if the values are equal (or if the type is complex and only existence is verified)
     */
    private boolean comparePDCValue(NamespacedKey key, ItemMeta sourceMeta, ItemMeta ingredientMeta) {
        var ingPDC = ingredientMeta.getPersistentDataContainer();
        var srcPDC = sourceMeta.getPersistentDataContainer();

        if (ingPDC.has(key, PersistentDataType.STRING)) {
            return Objects.equals(ingPDC.get(key, PersistentDataType.STRING),
                    srcPDC.get(key, PersistentDataType.STRING));
        }
        if (ingPDC.has(key, PersistentDataType.INTEGER)) {
            return Objects.equals(ingPDC.get(key, PersistentDataType.INTEGER),
                    srcPDC.get(key, PersistentDataType.INTEGER));
        }
        if (ingPDC.has(key, PersistentDataType.LONG)) {
            return Objects.equals(ingPDC.get(key, PersistentDataType.LONG),
                    srcPDC.get(key, PersistentDataType.LONG));
        }
        if (ingPDC.has(key, PersistentDataType.DOUBLE)) {
            return Objects.equals(ingPDC.get(key, PersistentDataType.DOUBLE),
                    srcPDC.get(key, PersistentDataType.DOUBLE));
        }
        if (ingPDC.has(key, PersistentDataType.FLOAT)) {
            return Objects.equals(ingPDC.get(key, PersistentDataType.FLOAT),
                    srcPDC.get(key, PersistentDataType.FLOAT));
        }
        if (ingPDC.has(key, PersistentDataType.BYTE)) {
            return Objects.equals(ingPDC.get(key, PersistentDataType.BYTE),
                    srcPDC.get(key, PersistentDataType.BYTE));
        }
        // Complex types (BYTE_ARRAY, TAG_CONTAINER, etc.): fall back to existence-only check
        return ingPDC.has(key) == srcPDC.has(key);
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
