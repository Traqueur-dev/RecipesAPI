package fr.traqueur.recipes.api.domains;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public interface Ingredient {

    boolean isSimilar(ItemStack item);

    RecipeChoice choice();

    Character sign();

}
