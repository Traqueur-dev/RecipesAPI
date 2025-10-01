package fr.traqueur.recipes.api;

import fr.traqueur.recipes.api.hook.Hook;
import fr.traqueur.recipes.impl.PrepareCraftListener;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import fr.traqueur.recipes.impl.updater.Updater;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * RecipesAPI is the main class of the API
 * It allows you to create and manage recipes
 */
public final class RecipesAPI {

    /**
     * The plugin instance
     */
    private final Plugin plugin;

    /**
     * If the debug mode is enabled
     */
    private final boolean debug;

    /**
     * The list of recipes
     */
    private final List<ItemRecipe> recipes;

    /**
     * Create a new instance of RecipesAPI
     * @param plugin The plugin instance
     * @param debug If the debug mode is enabled
     */
    public RecipesAPI(Plugin plugin, boolean debug) {
        this.debug = debug;
        this.plugin = plugin;
        this.recipes = new ArrayList<>();

        RecipeType.registerPlugin(plugin);

        plugin.getServer().getPluginManager().registerEvents(new PrepareCraftListener(this), plugin);

        if(this.debug) {
            Hook.HOOKS.stream()
                    .filter(Hook::isEnable)
                    .forEach(hook -> this.plugin.getLogger().info("Hook enabled: " + hook.getPluginName()));

            Updater.update("RecipesAPI");
        }
    }

    /**
     * Unregister all the recipes in the list of recipes from the server
     */
    public void unregisterRecipes() {
        for (ItemRecipe recipe : recipes) {
            plugin.getServer().removeRecipe(recipe.getKey());
        }
        recipes.clear();
    }

    /**
     * Add a recipe to the list of recipes
     * @param recipe The recipe to add
     */
    public void addRecipe(ItemRecipe recipe) {
        if (recipes.stream().anyMatch(r -> r.getKey().equals(recipe.getKey()))) {
            throw new IllegalArgumentException("Recipe already registered");
        }
        this.recipes.add(recipe);
        if(plugin.getServer().getRecipe(recipe.getKey()) == null) {
            plugin.getServer().addRecipe(recipe.toBukkitRecipe());
        }
        if(this.debug) {
            plugin.getLogger().info("Registering recipe: " + recipe.getKey());
        }
    }

    /**
     * Remove a recipe from the list of recipes
     * @param recipe The recipe to remove
     */
    public void removeRecipe(ItemRecipe recipe) {
        plugin.getServer().removeRecipe(recipe.getKey());
        this.recipes.remove(recipe);
        if(this.debug) {
            plugin.getLogger().info("Unregistering recipe: " + recipe.getKey());
        }
    }

    /**
     * Get the list of recipes
     * @return The list of recipes
     */
    public List<ItemRecipe> getRecipes() {
        return recipes;
    }

    /**
     * Get the plugin instance
     * @return The plugin instance
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Get if the debug mode is enabled
     * @return If the debug mode is enabled
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Log a debug message
     * @param message The message to log
     * @param args The arguments to format the message
     */
    public void debug(String message, Object... args) {
        String formattedMessage = String.format(message, args);
        if (debug) {
            this.plugin.getLogger().info(formattedMessage);
        }
    }

    /**
     * Create a new RecipeLoader instance for custom recipe loading
     * @return A new RecipeLoader instance
     */
    public RecipeLoader createLoader() {
        return new RecipeLoader(plugin, this);
    }
}