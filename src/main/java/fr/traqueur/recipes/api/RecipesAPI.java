package fr.traqueur.recipes.api;

import fr.traqueur.recipes.api.hook.Hook;
import fr.traqueur.recipes.impl.PrepareCraftListener;
import fr.traqueur.recipes.impl.domains.recipes.RecipeConfiguration;
import fr.traqueur.recipes.impl.RecipesListener;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import fr.traqueur.recipes.impl.updater.Updater;
import org.bukkit.Bukkit;
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
     * The scheduler
     */
    private final com.tcoded.folialib.impl.PlatformScheduler scheduler;


    /**
     * Create a new instance of RecipesAPI with yml support enabled
     * @param plugin The plugin instance
     * @param debug If the debug mode is enabled
     */
    public RecipesAPI(JavaPlugin plugin, boolean debug) {
        this(plugin, debug, true, null);
    }

    /**
     * Create a new instance of RecipesAPI
     * @param plugin The plugin instance
     * @param debug If the debug mode is enabled
     * @param enableYmlSupport If the yml support is enabled
     */
    public RecipesAPI(JavaPlugin plugin, boolean debug, boolean enableYmlSupport, com.tcoded.folialib.impl.PlatformScheduler scheduler) {
        this.debug = debug;
        this.plugin = plugin;
        this.recipes = new ArrayList<>();
        this.scheduler = scheduler;

        RecipeType.registerPlugin(plugin);

        plugin.getServer().getPluginManager().registerEvents(new PrepareCraftListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new RecipesListener(this), plugin);

        this.runNextTick(() -> {

            if(this.debug) {
                Hook.HOOKS.stream()
                        .filter(hook -> hook.isEnable(plugin))
                        .forEach(hook -> this.plugin.getLogger().info("Hook enabled: " + hook.getPluginName()));
            }

            if(enableYmlSupport) {
                var recipeFolder = new File(plugin.getDataFolder(), "recipes/");
                if (!recipeFolder.exists() && !recipeFolder.mkdirs()) {
                    plugin.getLogger().warning("Could not create recipes folder.");
                    return;
                }
                this.addConfiguredRecipes(recipeFolder);
            }
        });

        if(this.debug) {
            Updater.update("RecipesAPI");
        }
    }

    /**
     * Run a task on the next tick
     * @param runnable The task to run
     */
    private void runNextTick(Runnable runnable) {
        //Permits to use FoliaLib's scheduler if it's present in the plugin
        if(scheduler != null) {
            this.scheduler.runNextTick((t) -> runnable.run());
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, runnable, 1);
        }
    }

    /**
     * Add all the recipes in the recipe folder to the list of recipes
     * @param recipeFolder The folder containing the recipes
     */
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

    /**
     * Load a recipe from a file
     * @param file The file to load the recipe from
     */
    private void loadRecipe(File file) {
        if(!new File(this.plugin.getDataFolder(), "recipes/" + file.getName()).exists()) {
            this.plugin.saveResource("recipes/" + file.getName(), false);
        }
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        var recipe = new RecipeConfiguration(this.plugin, file.getName().replace(".yml", ""), configuration)
                .build();
        this.addRecipe(recipe);
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
     */
    public void addRecipe(ItemRecipe recipe) {
        this.recipes.add(recipe);
        plugin.getServer().addRecipe(recipe.toBukkitRecipe());
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