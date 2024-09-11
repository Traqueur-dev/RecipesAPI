package fr.traqueur.recipes.impl;

import fr.traqueur.recipes.api.RecipesAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

/**
 * This class is used to listen to events that are related to the api.
 */
public class RecipesListener implements Listener {

    /**
     * The API instance.
     */
    private final RecipesAPI api;

    /**
     * Creates a new RecipesListener instance.
     * @param api the API instance
     */
    public RecipesListener(RecipesAPI api) {
        this.api = api;
    }

    /**
     * This method is called when a plugin is disabled.
     * @param event the event
     */
    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        this.api.unregisterRecipes();
    }

}
