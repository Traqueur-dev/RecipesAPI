package fr.traqueur.recipes.impl.hook;

import dev.lone.itemsadder.api.CustomStack;
import fr.traqueur.recipes.api.domains.BaseIngredient;
import fr.traqueur.recipes.api.hook.Hook;
import fr.traqueur.recipes.impl.hook.hooks.ItemsAdderIngredient;
import fr.traqueur.recipes.impl.hook.hooks.OraxenIngredient;
import org.bukkit.inventory.ItemStack;

/**
 * This enum is used to define the different internal hooks that can be used in the plugin.
 */
public enum Hooks implements Hook {

    /**
     * The ItemsAdder hook.
     */
    ITEMSADDER {
        @Override
        public BaseIngredient getIngredient(String data, Character sign) {
            return new ItemsAdderIngredient(data, sign);
        }

        @Override
        public ItemStack getItemStack(String data) {
            if(!CustomStack.isInRegistry(data)) {
                throw new IllegalArgumentException("The item " + data + " is not registered in ItemsAdder.");
            }
            return CustomStack.getInstance(data).getItemStack();
        }
    },
    /**
     * The Oraxen hook.
     */
    ORAXEN {
        @Override
        public BaseIngredient getIngredient(String data, Character sign) {
            return new OraxenIngredient(data, sign);
        }

        @Override
        public ItemStack getItemStack(String data) {
            var builder = io.th0rgal.oraxen.api.OraxenItems.getItemById(data);
            if(builder == null) {
                throw new IllegalArgumentException("Oraxen item with id " + data + " not found");
            }
            return builder.build();
        }
    },
    ;

    /**
     * The name of the hook.
     */
    private final String name;

    /**
     * Constructor.
     * @param name The name of the hook.
     */
    Hooks(String name) {
        this.name = name;
    }

    /**
     * Default constructor.
     */
    Hooks() {
        this.name = this.name().toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginName() {
        return this.name;
    }
}
