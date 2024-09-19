package fr.traqueur.recipes.impl.hook.hooks;

import dev.lone.itemsadder.api.CustomStack;
import fr.traqueur.recipes.api.domains.BaseIngredient;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class ItemsAdderIngredient extends BaseIngredient {

    private final CustomStack customStack;

    public ItemsAdderIngredient(String data, Character sign) {
        super(sign);
        if(!CustomStack.isInRegistry(data)) {
            throw new IllegalArgumentException("The item " + data + " is not registered in ItemsAdder.");
        }
        this.customStack = CustomStack.getInstance(data);
    }

    @Override
    public boolean isSimilar(ItemStack ingredient) {
        return this.customStack.getItemStack().isSimilar(ingredient);
    }

    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(this.customStack.getItemStack().getType());
    }

}
