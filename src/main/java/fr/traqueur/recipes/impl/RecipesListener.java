package fr.traqueur.recipes.impl;

import fr.traqueur.recipes.api.RecipesAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class RecipesListener implements Listener {

    private final RecipesAPI api;

    public RecipesListener(RecipesAPI api) {
        this.api = api;
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        this.api.unregisterRecipes();
    }

}
