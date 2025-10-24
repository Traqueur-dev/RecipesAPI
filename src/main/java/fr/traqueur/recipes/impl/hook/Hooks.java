package fr.traqueur.recipes.impl.hook;

import dev.lone.itemsadder.api.CustomStack;
import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.api.hook.Hook;
import fr.traqueur.recipes.impl.hook.hooks.ItemsAdderIngredient;
import fr.traqueur.recipes.impl.hook.hooks.OraxenIngredient;
import org.bukkit.entity.Player;
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
        public Ingredient getIngredient(String data, Character sign) {
            return new ItemsAdderIngredient(data, sign);
        }

        @Override
        public ItemStack getItemStack(Player player, String data) {
            CustomStack stack = CustomStack.getInstance(data);
            if (stack == null) {
                throw new IllegalArgumentException("ItemsAdder item with id " + data + " not found");
            }
            return stack.getItemStack();
        }
    },
    /**
     * The Oraxen hook.
     */
    ORAXEN {
        @Override
        public Ingredient getIngredient(String data, Character sign) {
            return new OraxenIngredient(data, sign);
        }

        @Override
        public ItemStack getItemStack(Player player, String data) {
            var builder = io.th0rgal.oraxen.api.OraxenItems.getItemById(data);
            if(builder == null) {
                throw new IllegalArgumentException("Oraxen item with id " + data + " not found");
            }
            return builder.build();
        }
    },
    ;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPluginName() {
        return this.name().toLowerCase();
    }
}
