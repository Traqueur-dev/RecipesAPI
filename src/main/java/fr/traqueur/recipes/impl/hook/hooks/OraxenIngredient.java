package fr.traqueur.recipes.impl.hook.hooks;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class OraxenIngredient extends BaseIngredient {

    private final Material material;
    private final String id;

    public OraxenIngredient(String id, Character sign) {
        super(sign);
        var builder = OraxenItems.getItemById(id);
        if(builder == null) {
            throw new IllegalArgumentException("Oraxen item with id " + id + " not found");
        }
        this.material = builder.build().getType();
        this.id = id;
    }


    public OraxenIngredient(String id) {
        this(id, null);
    }

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

    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(material);
    }
}
