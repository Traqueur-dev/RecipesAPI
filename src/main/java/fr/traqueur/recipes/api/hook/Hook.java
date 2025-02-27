package fr.traqueur.recipes.api.hook;

import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.impl.hook.Hooks;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Hooks are used to create custom ingredients
 */
public interface Hook {

    /**
     * The list of hooks
     */
    List<Hook> HOOKS = new ArrayList<>(List.of(Hooks.values()));

    /**
     * Add a new hook
     * @param hook The hook to add
     */
    static void addHook(Hook hook) {
        HOOKS.add(hook);
    }

    /**
     * Get a plugin name of the hook
     * @return The hook's plugin name
     */
    String getPluginName();

    /**
     * Get an ingredient from the hook
     * @param data The data of the ingredient
     * @param sign The sign of the ingredient
     * @return The ingredient
     */
    Ingredient getIngredient(String data, Character sign);

    /**
     * Check if the plugin is enabled
     * @param plugin The plugin which use the API
     * @return If the plugin is enabled
     */
    default boolean isEnable(JavaPlugin plugin) {
        return plugin.getServer().getPluginManager().getPlugin(getPluginName()) != null;
    }

    ItemStack getItemStack(String resultPart);
}
