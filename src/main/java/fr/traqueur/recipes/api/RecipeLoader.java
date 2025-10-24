package fr.traqueur.recipes.api;

import fr.traqueur.recipes.impl.domains.ItemRecipe;
import fr.traqueur.recipes.impl.domains.recipes.RecipeConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

/**
 * RecipeLoader allows you to load recipes from multiple sources
 * using a fluent API.
 */
public class RecipeLoader {

    /**
     * The plugin instance
     */
    private final Plugin plugin;

    /**
     * The API instance to register recipes
     */
    private final RecipesAPI api;

    /**
     * List of folders to load recipes from
     */
    private final List<File> folders = new ArrayList<>();

    /**
     * List of individual files to load
     */
    private final List<File> files = new ArrayList<>();

    /**
     * Create a new RecipeLoader
     * Can be instantiated via RecipesAPI.createLoader()
     * @param plugin The plugin instance
     * @param api The RecipesAPI instance
     */
    protected RecipeLoader(Plugin plugin, RecipesAPI api) {
        this.plugin = plugin;
        this.api = api;
    }

    /**
     * Add a folder to load recipes from (recursive)
     * The path is relative to the plugin's data folder
     * If the folder doesn't exist, it will automatically extract default recipes from the JAR
     * @param path The path to the folder
     * @return This RecipeLoader instance for chaining
     */
    public RecipeLoader addFolder(String path) {
        File folder = new File(plugin.getDataFolder(), path);

        // If folder doesn't exist, extract defaults from JAR
        if (!folder.exists()) {
            // Create folder if extraction didn't create it
            if (!folder.mkdirs()) {
                plugin.getLogger().warning("Could not create folder: " + path);
                return this;
            }
            extractDefaultsFromJar(path);
        }

        if (!folder.isDirectory()) {
            plugin.getLogger().warning("Path is not a folder: " + path);
            return this;
        }
        this.folders.add(folder);
        return this;
    }

    /**
     * Add a file to load a recipe from
     * The path is relative to the plugin's data folder
     * @param path The path to the file
     * @return This RecipeLoader instance for chaining
     */
    public RecipeLoader addFile(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            plugin.getLogger().warning("File does not exist: " + path);
            return this;
        }
        if (!file.isFile()) {
            plugin.getLogger().warning("Path is not a file: " + path);
            return this;
        }
        if (!file.getName().endsWith(".yml")) {
            plugin.getLogger().warning("File is not a YAML file: " + path);
            return this;
        }
        this.files.add(file);
        return this;
    }

    /**
     * Extract default recipes from the JAR to the data folder
     * This will scan for .yml files in the specified JAR path and extract them
     * if they don't already exist in the data folder
     * @param jarPath The path inside the JAR to scan for recipes (e.g., "recipes/")
     */
    private void extractDefaultsFromJar(String jarPath) {
        if (!jarPath.endsWith("/")) {
            jarPath += "/";
        }

        try {
            CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                try (JarInputStream jarStream = new JarInputStream(jar.openStream())) {
                    JarEntry entry;
                    while ((entry = jarStream.getNextJarEntry()) != null) {
                        if (entry.getName().startsWith(jarPath) && entry.getName().endsWith(".yml")) {
                            File outFile = new File(plugin.getDataFolder(), entry.getName());
                            File parentDir = outFile.getParentFile();
                            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                                plugin.getLogger().warning("Could not create directory: " + parentDir.getAbsolutePath());
                                continue;
                            }
                            if (!outFile.exists()) {
                                plugin.saveResource(entry.getName(), false);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Could not extract default recipes from JAR: " + e.getMessage());
        }
    }

    /**
     * Load all recipes from the configured folders and files
     * @return The number of recipes loaded
     */
    public int load() {
        List<ItemRecipe> recipes = new ArrayList<>();

        // Load from folders
        for (File folder : folders) {
            loadFromFolder(folder, recipes);
        }

        // Load from individual files
        for (File file : files) {
            loadRecipe(file, recipes);
        }

        // Sort recipes by priority (higher priority first)
        recipes.sort((r1, r2) -> Integer.compare(r2.priority(), r1.priority()));

        // Register sorted recipes
        for (ItemRecipe recipe : recipes) {
            api.addRecipe(recipe);
        }

        plugin.getLogger().info("Loaded " + recipes.size() + " recipes via RecipeLoader.");
        return recipes.size();
    }

    /**
     * Reload all recipes from the configured folders and files
     * This will unregister all existing recipes and reload them
     * @return The number of recipes loaded
     */
    public int reload() {
        api.unregisterRecipes();
        return load();
    }

    /**
     * Load all recipes from a folder (recursive)
     * @param folder The folder to load recipes from
     * @param recipes The list to add loaded recipes to
     */
    private void loadFromFolder(File folder, List<ItemRecipe> recipes) {
        try (Stream<Path> stream = Files.walk(folder.toPath())) {
            List<File> ymlFiles = stream.map(Path::toFile)
                    .filter(File::isFile)
                    .filter(f -> f.getName().endsWith(".yml"))
                    .toList();

            for (File file : ymlFiles) {
                loadRecipe(file, recipes);
            }
        } catch (IOException exception) {
            plugin.getLogger().severe("Could not load recipes from folder " + folder.getAbsolutePath() + ": " + exception.getMessage());
        }
    }

    /**
     * Load a recipe from a file
     * @param file The file to load the recipe from
     * @param recipes The list to add the loaded recipe to
     */
    private void loadRecipe(File file, List<ItemRecipe> recipes) {
        try {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
            ItemRecipe recipe = new RecipeConfiguration(file.getName().replace(".yml", ""), configuration)
                    .build();
            recipes.add(recipe);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not load recipe from file " + file.getAbsolutePath() + ": " + e.getMessage());
            if (api.isDebug()) {
                e.printStackTrace();
            }
        }
    }
}