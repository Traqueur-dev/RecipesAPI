package fr.traqueur.recipes.api;

import com.tcoded.folialib.FoliaLib;
import fr.traqueur.recipes.impl.PrepareCraftListener;
import fr.traqueur.recipes.impl.domains.recipes.RecipeConfiguration;
import fr.traqueur.recipes.impl.RecipesListener;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import fr.traqueur.recipes.impl.updater.Updater;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * RecipesAPI is the main class of the API
 * It allows you to create and manage recipes
 */
public final class RecipesAPI {

    /**
     * The plugin instance
     */
    private final JavaPlugin plugin;

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
    public RecipesAPI(JavaPlugin plugin, boolean debug) {
        this.debug = debug;
        this.plugin = plugin;
        this.recipes = new ArrayList<>();

        RecipeType.registerPlugin(plugin);

        plugin.getServer().getPluginManager().registerEvents(new PrepareCraftListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new RecipesListener(this), plugin);

        var recipeFolder = new File(plugin.getDataFolder(), "recipes");
        if (!recipeFolder.exists() && !recipeFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create recipes folder.");
            return;
        }

        new FoliaLib(plugin).getScheduler().runNextTick((wrappedTask) -> this.addConfiguredRecipes(recipeFolder));

        if(this.debug) {
            Updater.update("RecipesAPI");
        }
    }

    private void addConfiguredRecipes(File recipeFolder) {
        try (Stream<Path> stream = Files.walk(recipeFolder.toPath())) {
            stream.skip(1)
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(e -> e.getName().endsWith(".yml"))
                    .forEach(this::loadRecipe);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void loadRecipe(File file) {
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        var recipe = new RecipeConfiguration(file.getName().replace(".yml", ""), configuration).build();
        this.addRecipe(recipe, true);
    }

    /**
     * Register all the recipes in the list of recipes to the server
     */
    public void registerRecipes() {
        for (ItemRecipe recipe : recipes) {
            plugin.getServer().addRecipe(recipe.toBukkitRecipe());
        }
        if(this.debug) {
            plugin.getLogger().info("Registered " + recipes.size() + " recipes.");
        }
    }

    /**
     * Unregister all the recipes in the list of recipes from the server
     */
    public void unregisterRecipes() {
        for (ItemRecipe recipe : recipes) {
            plugin.getServer().removeRecipe(recipe.getKey());
        }
    }

    /**
     * Add a recipe to the list of recipes
     * @param recipe The recipe to add
     * @param register If the recipe should be registered to the server
     */
    public void addRecipe(ItemRecipe recipe, boolean register) {
        if(this.debug) {
            plugin.getLogger().info("Adding recipe: " + recipe.getKey());
        }
        this.recipes.add(recipe);
        if(register) {
            if (this.debug) {
                plugin.getLogger().info("Registering recipe: " + recipe.getKey());
            }
            plugin.getServer().addRecipe(recipe.toBukkitRecipe());
        }
    }

    /**
     * Add a recipe to the list of recipes
     * @param recipe The recipe to add
     */
    public void addRecipe(ItemRecipe recipe) {
        this.addRecipe(recipe, false);
    }

    /**
     * Remove a recipe from the list of recipes
     * @param recipe The recipe to remove
     */
    public void removeRecipe(ItemRecipe recipe) {
        plugin.getServer().removeRecipe(recipe.getKey());
        this.recipes.remove(recipe);
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
    public JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Get if the debug mode is enabled
     * @return If the debug mode is enabled
     */
    public boolean isDebug() {
        return debug;
    }
}