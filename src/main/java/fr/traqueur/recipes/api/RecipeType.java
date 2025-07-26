package fr.traqueur.recipes.api;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Represents a type of recipe.
 */
public enum RecipeType {

    /**
     * A recipe that is crafted in a crafting table.
     */
    CRAFTING_SHAPED(9),
    /**
     * A recipe that is crafted in a crafting table without a specific shape.
     */
    CRAFTING_SHAPELESS(9),
    /**
     * A recipe that is crafted in a furnace.
     */
    BLASTING(1),
    /**
     * A recipe that is crafted in a campfire.
     */
    CAMPFIRE_COOKING(1),
    /**
     * A recipe that is crafted in a smoker.
     */
    SMOKING(1),
    /**
     * A recipe that is crafted in a stonecutter.
     */
    STONE_CUTTING(1),
    /**
     * A recipe that is crafted in a furnace.
     */
    SMELTING(1),
    /**
     * A recipe that is crafted in a smithing table.
     */
    SMITHING_TRANSFORM(3),
    ;

    /**
     * The plugin that registered this enum.
     */
    private static Plugin plugin;

    /**
     * The maximum number of ingredients that can be used in this recipe.
     */
    private final int maxIngredients;

    /**
     * Creates a new recipe type.
     * @param maxIngredients the maximum number of ingredients that can be used in this recipe
     */
    RecipeType(int maxIngredients) {
        this.maxIngredients = maxIngredients;
    }

    /**
     * Gets the maximum number of ingredients that can be used in this recipe.
     * @return the maximum number of ingredients
     */
    public int getMaxIngredients() {
        return maxIngredients;
    }

    /**
     * Gets the namespaced key for a recipe with the given key.
     * @param key the key
     * @return the namespaced key
     */
    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(plugin, name().toLowerCase() + "_" + key);
    }

    /**
     * Registers the plugin that is using this enum.
     * @param plugin the plugin
     */
    public static void registerPlugin(Plugin plugin) {
        RecipeType.plugin = plugin;
    }

    /**
     * Gets a list of all the crafting recipes in "smelting" type.
     * @return a list of "smeltings" recipes
     */
    public static List<RecipeType> smeltingRecipes() {
        return List.of(CAMPFIRE_COOKING, BLASTING, SMOKING, SMELTING);
    }
}
