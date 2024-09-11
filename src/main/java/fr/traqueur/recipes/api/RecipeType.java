package fr.traqueur.recipes.api;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public enum RecipeType {

    CRAFTING_SHAPED(9),
    CRAFTING_SHAPELESS(9),
    BLASTING(1),
    CAMPFIRE_COOKING(1),
    SMOKING(1),
    STONE_CUTTING(1),
    SMELTING(1),
    SMITHING_TRANSFORM(3),
    ;

    private static JavaPlugin plugin;

    private final int maxIngredients;

    RecipeType(int maxIngredients) {
        this.maxIngredients = maxIngredients;
    }

    public int getMaxIngredients() {
        return maxIngredients;
    }

    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(plugin, name().toLowerCase() + "_" + key);
    }

    public static void registerPlugin(JavaPlugin plugin) {
        RecipeType.plugin = plugin;
    }

    public static List<RecipeType> smeltingRecipes() {
        return List.of(CAMPFIRE_COOKING, BLASTING, SMOKING, SMELTING);
    }
}
