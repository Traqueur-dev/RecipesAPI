package fr.traqueur.recipes.api;

import fr.traqueur.recipes.impl.updater.Updater;
import org.bukkit.plugin.java.JavaPlugin;

public final class RecipesAPI {

    private final JavaPlugin plugin;
    private boolean debug;

    public RecipesAPI(JavaPlugin plugin, boolean debug) {
        this.debug = debug;
        this.plugin = plugin;

        RecipeType.registerPlugin(plugin);

        if(this.debug) {
            Updater.update("RecipesAPI");
        }
    }

    public boolean isDebug() {
        return debug;
    }
}