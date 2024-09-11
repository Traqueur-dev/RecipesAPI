package fr.traqueur.recipes.api;

import fr.traqueur.recipes.api.domains.Recipe;
import fr.traqueur.recipes.impl.PrepareCraftListener;
import fr.traqueur.recipes.impl.RecipesListener;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import fr.traqueur.recipes.impl.updater.Updater;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class RecipesAPI {

    private final JavaPlugin plugin;
    private final boolean debug;
    private final List<ItemRecipe> recipes;

    public RecipesAPI(JavaPlugin plugin, boolean debug) {
        this.debug = debug;
        this.plugin = plugin;
        this.recipes = new ArrayList<>();

        RecipeType.registerPlugin(plugin);

        plugin.getServer().getPluginManager().registerEvents(new PrepareCraftListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new RecipesListener(this), plugin);

        if(this.debug) {
            Updater.update("RecipesAPI");
        }
    }

    public void registerRecipes() {
        for (ItemRecipe recipe : recipes) {
            plugin.getServer().addRecipe(recipe.toBukkitRecipe());
        }
        if(this.debug) {
            plugin.getLogger().info("Registered " + recipes.size() + " recipes.");
        }
    }

    public void unregisterRecipes() {
        for (ItemRecipe recipe : recipes) {
            plugin.getServer().removeRecipe(recipe.getKey());
        }
    }

    public void addRecipe(ItemRecipe recipe) {
        if(this.debug) {
            plugin.getLogger().info("Adding recipe: " + recipe.getKey());
        }
        this.recipes.add(recipe);
    }

    public void removeRecipe(ItemRecipe recipe) {
        plugin.getServer().removeRecipe(recipe.getKey());
        this.recipes.remove(recipe);
    }

    public List<ItemRecipe> getRecipes() {
        return recipes;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public boolean isDebug() {
        return debug;
    }
}