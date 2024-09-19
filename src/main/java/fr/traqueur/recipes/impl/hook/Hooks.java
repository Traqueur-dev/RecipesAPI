package fr.traqueur.recipes.impl.hook;

import fr.traqueur.recipes.api.domains.BaseIngredient;
import fr.traqueur.recipes.api.hook.Hook;
import fr.traqueur.recipes.impl.hook.hooks.ItemsAdderIngredient;
import fr.traqueur.recipes.impl.hook.hooks.OraxenIngredient;

public enum Hooks implements Hook {

    ITEMSADDER {
        @Override
        public BaseIngredient getIngredient(String data, Character sign) {
            return new ItemsAdderIngredient(data, sign);
        }
    },
    ORAXEN {
        @Override
        public BaseIngredient getIngredient(String data, Character sign) {
            return new OraxenIngredient(data, sign);
        }
    },
    ;

    private final String name;

    Hooks(String name) {
        this.name = name;
    }

    Hooks() {
        this.name = this.name().toLowerCase();
    }

    @Override
    public String getPluginName() {
        return this.name();
    }
}
