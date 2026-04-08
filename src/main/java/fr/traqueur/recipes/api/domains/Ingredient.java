package fr.traqueur.recipes.api.domains;

import fr.traqueur.recipes.api.hook.Hook;
import fr.traqueur.recipes.impl.domains.ingredients.ItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.MaterialIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.StrictItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.TagIngredient;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

/**
 * Base class for ingredients.
 */
public abstract class Ingredient {

    /**
     * The sign of the ingredient.
     */
    private final Character sign;

    /**
     * Constructor.
     * @param sign The sign of the ingredient.
     */
    public Ingredient(Character sign) {
        this.sign = sign;
    }

    /**
     * Get the sign of the ingredient.
     * @return The sign of the ingredient.
     */
    public Character sign() {
        return this.sign;
    }


    /**
     * Check if the item is similar to the ingredient.
     * @param item The item to check.
     * @return true if the item is similar to the ingredient, false otherwise.
     */
    public abstract boolean isSimilar(ItemStack item);

    /**
     * Get the choice of the ingredient.
     * @return The choice of the ingredient.
     */
    public abstract RecipeChoice choice();

    // ── Factory methods ────────────────────────────────────────────────────────

    /** Create a material-based ingredient (matches any item of that type). */
    public static Ingredient of(Material material) {
        return new MaterialIngredient(material, null);
    }

    /** Create a material-based ingredient with a shaped-recipe sign. */
    public static Ingredient of(Material material, Character sign) {
        return new MaterialIngredient(material, sign);
    }

    /** Create an ItemStack-based ingredient (checks meta, PDC, lore…). */
    public static Ingredient of(ItemStack item) {
        return new ItemStackIngredient(item, null);
    }

    /** Create an ItemStack-based ingredient with a shaped-recipe sign. */
    public static Ingredient of(ItemStack item, Character sign) {
        return new ItemStackIngredient(item, sign);
    }

    /**
     * Create an ItemStack-based ingredient.
     * @param strict if true, uses Bukkit's native {@code ItemStack.isSimilar()} (exact meta match)
     */
    public static Ingredient of(ItemStack item, boolean strict) {
        return strict ? new StrictItemStackIngredient(item, null) : new ItemStackIngredient(item, null);
    }

    /**
     * Create an ItemStack-based ingredient with a shaped-recipe sign.
     * @param strict if true, uses Bukkit's native {@code ItemStack.isSimilar()} (exact meta match)
     */
    public static Ingredient of(ItemStack item, Character sign, boolean strict) {
        return strict ? new StrictItemStackIngredient(item, sign) : new ItemStackIngredient(item, sign);
    }

    /** Create a tag-based ingredient (matches any item in the given Bukkit tag). */
    public static Ingredient of(Tag<Material> tag) {
        return new TagIngredient(tag, null);
    }

    /** Create a tag-based ingredient with a shaped-recipe sign. */
    public static Ingredient of(Tag<Material> tag, Character sign) {
        return new TagIngredient(tag, sign);
    }

    /**
     * Create an ingredient from a hook plugin (e.g. {@code "oraxen"} or {@code "itemsadder"}).
     * The hook must be enabled on the server.
     * @param pluginName the hook plugin name (case-insensitive)
     * @param itemId     the custom item id recognised by that plugin
     */
    public static Ingredient ofHook(String pluginName, String itemId) {
        return ofHook(pluginName, itemId, null);
    }

    /**
     * Create a hook-based ingredient with a shaped-recipe sign.
     * @param pluginName the hook plugin name (case-insensitive)
     * @param itemId     the custom item id recognised by that plugin
     * @param sign       the shaped-recipe sign, or {@code null} for shapeless
     */
    public static Ingredient ofHook(String pluginName, String itemId, Character sign) {
        return Hook.HOOKS.stream()
                .filter(Hook::isEnable)
                .filter(h -> h.getPluginName().equalsIgnoreCase(pluginName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enabled hook found for plugin: " + pluginName))
                .getIngredient(itemId, sign);
    }
}
