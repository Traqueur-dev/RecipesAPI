package fr.traqueur.recipes.impl.domains.recipes;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.api.Util;
import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.api.domains.Recipe;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to build recipes.
 */
public class RecipeBuilder implements Recipe {

    /**
     * The list of ingredients.
     */
    private final List<Ingredient> ingredientList = new ArrayList<>();

    /**
     * The name of the recipe.
     */
    private String name;

    /**
     * The result of the recipe.
     */
    private String result;

    /**
     * The amount of the result.
     */
    private int amount = 1;

    /**
     * The type of the recipe.
     */
    private RecipeType type;

    /**
     * The group of the recipe.
     */
    private String group = "";

    /**
     * The category of the recipe.
     */
    private String category = "";

    /**
     * The cooking time of the recipe.
     */
    private int cookingTime = 0;

    /**
     * The experience of the recipe.
     */
    private float experience = 0;

    /**
     * The priority of the recipe (higher = registered first).
     */
    private int priority = 0;

    /**
     * The pattern of the recipe.
     */
    private String[] pattern = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setName(String name) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        this.name = name;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setResult(ItemStack result) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        this.result = Util.fromItemStack(result);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setType(RecipeType type) {
        this.type = type;
        return this;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setGroup(String group) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        this.group = group;
        return this;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * Set the priority of the recipe (higher = registered first).
     * @param priority The priority of the recipe.
     * @return The recipe.
     */
    public Recipe setPriority(int priority) {
        if(type == null) {
            throw new IllegalArgumentException("Recipe type is not set");
        }
        this.priority = priority;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecipeType getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
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

        return this.getItemRecipe(ingredientList, type, pattern, cookingTime, name, group, category, result, amount, experience, priority);
    }
}
