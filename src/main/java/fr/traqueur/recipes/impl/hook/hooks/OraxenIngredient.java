package fr.traqueur.recipes.impl.hook.hooks;

import fr.traqueur.recipes.api.domains.Ingredient;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * This class is an implementation of the BaseIngredient class.
 * It is used to represent an ingredient that is an item from the Oraxen plugin.
 */
public class OraxenIngredient extends Ingredient {

    /**
     * The Material object that represents the item from Oraxen.
     */
    private final Material material;
    /**
     * The id of the item from Oraxen.
     */
    private final String id;

    /**
     * Constructor of the class.
     * @param id The id of the item from Oraxen.
     * @param sign The sign that represents the ingredient in the recipe.
     */
    public OraxenIngredient(String id, Character sign) {
        super(sign);
        var builder = OraxenItems.getItemById(id);
        if(builder == null) {
            throw new IllegalArgumentException("Oraxen item with id " + id + " not found");
        }
        this.material = builder.build().getType();
        this.id = id;
    }

    /**
     * Constructor of the class.
     * @param id The id of the item from Oraxen.
     */
    public OraxenIngredient(String id) {
        this(id, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(ItemStack item) {
        if (!item.hasItemMeta() || item.getItemMeta().getPersistentDataContainer().isEmpty()) {
            return false;
        }

        if(item.getType() != material) {
            return false;
        }

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if(container.has(OraxenItems.ITEM_ID, PersistentDataType.STRING)) {
            return container.getOrDefault(OraxenItems.ITEM_ID, PersistentDataType.STRING, "ERROR")
                    .equals(id);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(material);
    }
}
