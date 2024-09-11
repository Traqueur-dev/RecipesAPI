package fr.traqueur.recipes.api;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public enum RecipeType {

    CRAFTING_SHAPED,
    CRAFTING_SHAPELESS,
    BLASTING,
    CAMPFIRE_COOKING,
    SMOKING,
    STONE_CUTTING,
    SMELTING,
    SMITHING_TRANSFORM,
    ;

    private static JavaPlugin plugin;

    public static void registerPlugin(JavaPlugin plugin) {
        RecipeType.plugin = plugin;
    }


    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(plugin, name().toLowerCase() + "_" + key);
    }

    public static List<RecipeType> smeltingRecipes() {
        return List.of(CAMPFIRE_COOKING, BLASTING, SMOKING, SMELTING);
    }
}
