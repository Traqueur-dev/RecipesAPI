package fr.traqueur.recipes.impl.hook.hooks;

import dev.lone.itemsadder.api.CustomStack;
import fr.traqueur.recipes.api.domains.Ingredient;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

/**
 * This class is an implementation of the BaseIngredient class.
 * It is used to represent an ingredient that is an item from the ItemsAdder plugin.
 */
public class ItemsAdderIngredient extends Ingredient {

    /**
     * The CustomStack object that represents the item from ItemsAdder.
     */
    private final CustomStack customStack;

    /**
     * Constructor of the class.
     * @param data The id of the item from ItemsAdder.
     * @param sign The sign that represents the ingredient in the recipe.
     */
    public ItemsAdderIngredient(String data, Character sign) {
        super(sign);
        this.customStack = CustomStack.getInstance(data);
        if(this.customStack == null) {
            throw new IllegalArgumentException("The item " + data + " is not registered in ItemsAdder.");
        }
    }

    /**
     * Constructor of the class.
     * @param data The id of the item from ItemsAdder.
     */
    public ItemsAdderIngredient(String data) {
        this(data,null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSimilar(ItemStack ingredient) {
        return this.customStack.getItemStack().isSimilar(ingredient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecipeChoice choice() {
        return new RecipeChoice.MaterialChoice(this.customStack.getItemStack().getType());
    }

}
