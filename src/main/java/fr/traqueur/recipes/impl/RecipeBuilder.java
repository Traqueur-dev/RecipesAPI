package fr.traqueur.recipes.impl;

import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.api.domains.Recipe;
import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to build recipes.
 */
public class RecipeBuilder implements Recipe {

    private String name;
    private ItemStack result;
    private int amount = 1;
    private RecipeType type = null;
    private List<Ingredient> ingredientList = new ArrayList<>();
    private String group = "";
    private String category = "";
    private String[] pattern = null;
    private int cookingTime = 0;
    private float experience = 0;

    @Override
    public Recipe setName(String name) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        this.name = name;
        return this;
    }

    @Override
    public Recipe setResult(ItemStack result) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        this.result = result;
        return this;
    }

    @Override
    public Recipe setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public Recipe setType(RecipeType type) {
        this.type = type;
        return this;
    }

    @Override
    public Recipe addIngredient(Ingredient ingredient) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        if(type.getMaxIngredients() <= ingredientList.size()) {
            throw new IllegalArgumentException("Too many ingredients");
        }

        if(type == RecipeType.CRAFTING_SHAPED) {
            if(pattern == null) {
                throw new IllegalArgumentException("Pattern is not set");
            }
            if(ingredient.sign() == null) {
                throw new IllegalArgumentException("Ingredient sign is not set");
            }

            if(Arrays.stream(pattern)
                    .flatMapToInt(String::chars)
                    .noneMatch(c -> c == ingredient.sign())) {
                throw new IllegalArgumentException("Pattern does not contain the ingredient sign");
            }
        }

        this.ingredientList.add(ingredient);
        return this;
    }

    @Override
    public Recipe setGroup(String group) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        this.group = group;
        return this;
    }

    @Override
    public Recipe setCategory(String category) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        if(type == RecipeType.STONE_CUTTING) {
            throw new IllegalArgumentException("Category is not valid for STONE_CUTTING type");
        }

        this.category = category;
        return this;
    }

    @Override
    public Recipe setPattern(String[] pattern) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        if (type != RecipeType.CRAFTING_SHAPED) {
            throw new IllegalArgumentException("Recipe type is not a shaped recipe");
        }
        if(pattern.length > 3) {
            throw new IllegalArgumentException("Pattern is too long");
        }

        boolean areLengthsValid = Arrays.stream(pattern)
                .map(String::length)
                .allMatch(len -> len >= 1 && len <= 3) &&
                Arrays.stream(pattern)
                        .map(String::length)
                        .distinct()
                        .count() == 1;

        if(!areLengthsValid) {
            throw new IllegalArgumentException("Pattern is not valid");
        }

        this.pattern = pattern;
        return this;
    }

    @Override
    public Recipe setCookingTime(int cookingTime) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        if(!RecipeType.smeltingRecipes().contains(type)) {
            throw new IllegalArgumentException("Recipe type is not a smelting recipe");
        }
        this.cookingTime = cookingTime;
        return this;
    }

    @Override
    public Recipe setExperience(float experience) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        if(!RecipeType.smeltingRecipes().contains(type)) {
            throw new IllegalArgumentException("Recipe type is not a smelting recipe");
        }
        this.experience = experience;
        return this;
    }

    @Override
    public RecipeType getType() {
        return type;
    }

    @Override
    public ItemRecipe build() {
        if (name == null) {
            throw new IllegalArgumentException("Name is not set");
        }

        if (result == null) {
            throw new IllegalArgumentException("Result is not set");
        }

        if (type == null) {
            throw new IllegalArgumentException("Type is not set");
        }

        if (ingredientList.isEmpty()) {
            throw new IllegalArgumentException("Ingredients are not set");
        }

        if (type == RecipeType.CRAFTING_SHAPED && pattern == null) {
            throw new IllegalArgumentException("Pattern is not set");
        }

        if (type == RecipeType.CRAFTING_SHAPED && pattern.length == 0) {
            throw new IllegalArgumentException("Pattern is empty");
        }

        if(RecipeType.smeltingRecipes().contains(type) && cookingTime == 0) {
            throw new IllegalArgumentException("Cooking time is not set");
        }

        return new ItemRecipe(name, group, category, type, result, amount, ingredientList.toArray(new Ingredient[0]), pattern, cookingTime, experience);
    }
}
