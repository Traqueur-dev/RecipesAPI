package fr.traqueur.recipes.api.hook;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import fr.traqueur.recipes.impl.hook.Hooks;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public interface Hook {

    List<Hook> HOOKS = new ArrayList<>(List.of(Hooks.values()));

    static void addHook(Hook hook) {
        HOOKS.add(hook);
    }

    String getPluginName();

    BaseIngredient getIngredient(String data, Character sign);

    default boolean isEnable(JavaPlugin plugin) {
        return plugin.getServer().getPluginManager().isPluginEnabled(getPluginName());
    }

}
