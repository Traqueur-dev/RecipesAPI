package fr.traqueur.recipes.api.domains;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import fr.traqueur.recipes.impl.domains.ingredients.ItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.MaterialIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.StrictItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.TagIngredient;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

public interface Recipe {

    Recipe setName(String name);

    Recipe setResult(ItemStack result);

    Recipe setAmount(int amount);

    Recipe setType(RecipeType type);

    default Recipe addIngredient(Ingredient... ingredients) {
        for (Ingredient ingredient : ingredients) {
            addIngredient(ingredient);
        }
        return this;
    }

    default Recipe addIngredient(Tag<Material> tag) {
        return addIngredient(tag, null);
    }

    default Recipe addIngredient(Tag<Material> tag, Character sign) {
        return addIngredient(new TagIngredient(tag, sign));
    }

    default Recipe addIngredient(ItemStack item) {
        if(this.getType() == RecipeType.CRAFTING_SHAPED) {
            throw new UnsupportedOperationException("You can't add an ingredient withou sign to a shaped recipe");
        }
        return addIngredient(item, null);
    }

    default Recipe addIngredient(ItemStack item, boolean strict) {
        return addIngredient(item, null, strict);
    }

    default Recipe addIngredient(ItemStack item, Character sign) {
        return addIngredient(item, sign, false);
    }

    default Recipe addIngredient(ItemStack item, Character sign, boolean strict) {
        return addIngredient(strict ? new StrictItemStackIngredient(item, sign)
                : new ItemStackIngredient(item, sign));
    }

    default Recipe addIngredient(Material material) {
        if(this.getType() == RecipeType.CRAFTING_SHAPED) {
            throw new UnsupportedOperationException("You can't add an ingredient without sign to a shaped recipe");
        }
        return addIngredient(material, null);
    }

    default Recipe addIngredient(Material material, Character sign) {
        return addIngredient(new MaterialIngredient(material, sign));
    }

    Recipe addIngredient(Ingredient ingredient);

    Recipe setGroup(String group);

    Recipe setCategory(String category);

    Recipe setPattern(String... pattern);

    Recipe setCookingTime(int cookingTime);

    Recipe setExperience(float experience);

    RecipeType getType();

    ItemRecipe build();

}
