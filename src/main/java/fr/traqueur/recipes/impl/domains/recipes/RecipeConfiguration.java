package fr.traqueur.recipes.impl.domains.recipes;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.api.TagRegistry;
import fr.traqueur.recipes.api.Util;
import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.api.domains.Recipe;
import fr.traqueur.recipes.api.hook.Hook;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import fr.traqueur.recipes.impl.domains.ingredients.ItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.MaterialIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.StrictItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.TagIngredient;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

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

            String[] data = material.split(":");
            if(data.length == 1) {
                this.ingredientList.add(new MaterialIngredient(Util.getMaterial(data[0]), sign));
            } else {
               Ingredient ingred = switch (data[0]) {
                   case "material" -> new MaterialIngredient(Util.getMaterial(data[1]), sign);
                   case "tag" -> new TagIngredient(this.getTag(data[1]), sign);
                   case "item" -> {
                       boolean strict = this.isStrict(ingredient);
                       if(strict) {
                           yield new StrictItemStackIngredient(Util.getItemStack(data[1]), sign);
                       }
                       yield new ItemStackIngredient(Util.getItemStack(data[1]), sign);
                   }
                   default -> Hook.HOOKS.stream()
                           .filter(Hook::isEnable)
                           .filter(hook -> hook.getPluginName().equalsIgnoreCase(data[0]))
                           .findFirst()
                           .orElseThrow(() -> new IllegalArgumentException("The data " + data[0] + " isn't valid."))
                           .getIngredient(data[1], sign);
               };
               this.ingredientList.add(ingred);
            }

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
    }

    /**
     * This method is used to get Tag from the string.
     * @param data the data to get the tag.
     * @return the tag.
     */
    private Tag<Material> getTag(String data) {
        return TagRegistry.getTag(data).orElseThrow(() -> new IllegalArgumentException("The tag " + data + " isn't valid."));
    }

    /**
     * This method is used to check if the ingredient is strict.
     * @param ingredient the ingredient to check.
     */
    private boolean isStrict(Map<?,?> ingredient) {
        return ingredient.containsKey("strict") && (boolean) ingredient.get("strict");
    }

    /**
     * This method is used to check if the category is valid.
     * @param category the group to check.
     * @return true if the category is valid.
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
        return this.getItemRecipe(ingredientList, type, pattern, cookingTime, name, group, category, resultStr, amount, experience);
    }
}
