package fr.traqueur.recipes.impl.domains.recipes;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.api.Util;
import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.api.domains.Recipe;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class is used to build recipes via yaml configuration.
 */
public class RecipeConfiguration implements Recipe {

    /**
     * The list of ingredients.
     */
    private final List<Ingredient> ingredientList = new ArrayList<>();

    /**
     * The name of the recipe.
     */
    private final String name;

    /**
     * The result of the recipe.
     */
    private final String resultStr;

    /**
     * The amount of the result.
     */
    private final int amount;

    /**
     * The type of the recipe.
     */
    private final RecipeType type;

    /**
     * The group of the recipe.
     */
    private final String group;

    /**
     * The category of the recipe.
     */
    private final String category;

    /**
     * The pattern of the recipe.
     */
    private final int cookingTime;

    /**
     * The experience of the recipe.
     */
    private final float experience;

    /**
     * The priority of the recipe (higher = registered first).
     */
    private final int priority;

    /**
     * The pattern of the recipe.
     */
    private String[] pattern = null;

    /**
     * The constructor of the recipe.
     * @param name the name of the recipe.
     * @param configuration the configuration of the recipe.
     */
    public RecipeConfiguration(String name, YamlConfiguration configuration) {
        this(name, "", configuration);
    }

    /**
     * The constructor of the recipe.
     * @param name the name of the recipe.
     * @param path the path of the recipe.
     * @param configuration the configuration of the recipe.
     */
    public RecipeConfiguration(String name, String path, YamlConfiguration configuration) {
        this.name = name.replace(".yml", "");
        if(!path.endsWith(".") && !path.isEmpty()) {
            path += ".";
        }
        String strType = configuration.getString(path + "type", "ERROR");
        try {
            this.type = RecipeType.valueOf(strType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The type " + strType + " isn't valid.");
        }
        this.category = configuration.getString(path + "category", "");
        this.group = configuration.getString(path + "group", "");
        if(!this.checkCategory(this.category)) {
            throw new IllegalArgumentException("The category " + this.category + " isn't valid.");
        }

        if(configuration.contains(path + "pattern")) {
            this.pattern = configuration.getStringList(path+"pattern").toArray(new String[0]);
            this.validatePattern();
        }

        if(!configuration.contains(path + "ingredients")) {
            throw new IllegalArgumentException("The recipe " + name + " doesn't have ingredients.");
        }

        for(Map<?,?> ingredient : configuration.getMapList(path + "ingredients")) {
            String material = (String) ingredient.get("item");
            var objSign = ingredient.getOrDefault("sign", null);
            Character sign = objSign == null ? null : objSign.toString().charAt(0);
            boolean strict = this.isStrict(ingredient);

            Ingredient ingred = Util.parseIngredient(material, sign, strict);
            this.ingredientList.add(ingred);
        }

        if(!configuration.contains(path + "result.item")) {
            throw new IllegalArgumentException("The recipe " + name + " doesn't have a result.");
        }
        String strItem = configuration.getString(path + "result.item");
        if (strItem == null) {
            throw new IllegalArgumentException("The recipe " + name + " doesn't have a result.");
        }
        this.resultStr = strItem;
        this.amount = configuration.getInt(path + "result.amount", 1);


        this.cookingTime = configuration.getInt(path + "cooking-time", 0);
        this.experience = (float) configuration.getDouble(path + "experience", 0d);
        this.priority = configuration.getInt(path + "priority", 0);
    }

    /**
     * This method is used to check if the ingredient is strict.
     * @param ingredient the ingredient to check.
     */
    private boolean isStrict(Map<?,?> ingredient) {
        return ingredient.containsKey("strict") && (boolean) ingredient.get("strict");
    }

    /**
     * This method is used to validate the pattern.
     * It checks if the pattern is valid for a shaped recipe.
     */
    private boolean checkCategory(@NotNull String category) {
        if(category.isEmpty()) {
            return true;
        }

        String upperCategory = category.toUpperCase();

        for(CookingBookCategory cookingCategory : CookingBookCategory.values()) {
            if(cookingCategory.name().equals(upperCategory)) {
                return true;
            }
        }

        for(CraftingBookCategory craftingCategory : CraftingBookCategory.values()) {
            if(craftingCategory.name().equals(upperCategory)) {
                return true;
            }
        }

        return false;
    }

    /**
     * This method is used to validate the pattern.
     * It checks if the pattern is valid for a shaped recipe.
     */
    private void validatePattern() {
        if (this.pattern == null || this.pattern.length == 0) {
            throw new IllegalArgumentException("The recipe " + name + " has an empty pattern.");
        }

        // Validate pattern size (max 3 rows)
        if (this.pattern.length > 3) {
            throw new IllegalArgumentException("The recipe " + name + " has a pattern with more than 3 rows.");
        }

        // Validate each row length (max 3 characters) and collect all characters
        Set<Character> patternChars = new HashSet<>();
        for (int i = 0; i < this.pattern.length; i++) {
            String row = this.pattern[i];
            if (row.length() > 3) {
                throw new IllegalArgumentException("The recipe " + name + " has a pattern row '" + row + "' with more than 3 characters.");
            }
            if (row.isEmpty()) {
                throw new IllegalArgumentException("The recipe " + name + " has an empty pattern row at index " + i + ".");
            }
            // Collect all non-space characters
            for (char c : row.toCharArray()) {
                if (c != ' ') {
                    patternChars.add(c);
                }
            }
        }

        // Validate that all pattern characters will have corresponding ingredients
        if (!patternChars.isEmpty()) {
            Set<Character> ingredientSigns = new HashSet<>();
            for (Ingredient ingredient : ingredientList) {
                if (ingredient.sign() != null) {
                    ingredientSigns.add(ingredient.sign());
                }
            }

            for (Character patternChar : patternChars) {
                if (!ingredientSigns.contains(patternChar)) {
                    throw new IllegalArgumentException("The recipe " + name + " has a pattern character '" + patternChar + "' that doesn't match any ingredient sign.");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setResult(ItemStack result) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setAmount(int amount) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setType(RecipeType type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe addIngredient(Ingredient ingredient) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setGroup(String group) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setCategory(String category) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setPattern(String... pattern) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setCookingTime(int cookingTime) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Recipe setExperience(float experience) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecipeType getType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemRecipe build() {
        return this.getItemRecipe(ingredientList, type, pattern, cookingTime, name, group, category, resultStr, amount, experience, priority);
    }

    /**
     * Get the priority of the recipe.
     * @return the priority of the recipe.
     */
    public int getPriority() {
        return priority;
    }
}
