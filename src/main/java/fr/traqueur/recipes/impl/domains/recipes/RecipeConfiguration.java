package fr.traqueur.recipes.impl.domains.recipes;

import fr.traqueur.recipes.api.Base64;
import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.api.TagRegistry;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final ItemStack result;

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
     * @param plugin the plugin of the recipe.
     * @param name the name of the recipe.
     * @param configuration the configuration of the recipe.
     */
    public RecipeConfiguration(JavaPlugin plugin, String name, YamlConfiguration configuration) {
        this(plugin, name, "", configuration);
    }

    /**
     * The constructor of the recipe.
     * @param plugin the plugin of the recipe.
     * @param name the name of the recipe.
     * @param path the path of the recipe.
     * @param configuration the configuration of the recipe.
     */
    public RecipeConfiguration(JavaPlugin plugin, String name, String path, YamlConfiguration configuration) {
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
        if(!this.checkGategory(this.category)) {
            throw new IllegalArgumentException("The category " + this.category + " isn't valid.");
        }

        if(configuration.contains(path + "pattern")) {
            this.pattern = configuration.getStringList(path+"pattern").toArray(new String[0]);
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
                this.ingredientList.add(new MaterialIngredient(this.getMaterial(data[0]), sign));
            } else {
               Ingredient ingred = switch (data[0]) {
                   case "material" -> new MaterialIngredient(this.getMaterial(data[1]), sign);
                   case "tag" -> new TagIngredient(this.getTag(data[1]), sign);
                   case "item" -> {
                       boolean strict = this.isStrict(ingredient);
                       if(strict) {
                           yield new StrictItemStackIngredient(this.getItemStack(data[1]), sign);
                       }
                       yield new ItemStackIngredient(this.getItemStack(data[1]), sign);
                   }
                   default -> Hook.HOOKS.stream()
                           .filter(hook -> hook.isEnable(plugin))
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
        String[] resultParts = strItem.split(":");
        if(resultParts.length == 1) {
            this.result = this.getItemStack(resultParts[0]);
        } else {
            this.result = switch (resultParts[0]) {
                case "material" -> new ItemStack(this.getMaterial(resultParts[1]));
                case "item", "base64" -> this.getItemStack(resultParts[1]);
                default -> Hook.HOOKS.stream()
                        .filter(hook -> hook.isEnable(plugin))
                        .filter(hook -> hook.getPluginName().equalsIgnoreCase(resultParts[0]))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("The result " + strItem + " isn't valid."))
                        .getItemStack(resultParts[1]);
            };
        }
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
     * This method is used to get the itemstack from base64 string
     * @param base64itemstack the base64 item stack.
     * @return the item stack.
     */
    private ItemStack getItemStack(String base64itemstack) {
        return Base64.decodeItem(base64itemstack);
    }

    /**
     * This method is used to get the material from the string.
     * @param material the material string.
     * @return the material.
     */
    private Material getMaterial(String material) {
        try {
            return Material.valueOf(material.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The material " + material + " isn't valid.");
        }
    }

    /**
     * This method is used to check if the category is valid.
     * @param category the group to check.
     * @return true if the category is valid.
     */
    private boolean checkGategory(String category) {
        category = category.toUpperCase();
        try {
            CookingBookCategory.valueOf(category);
        } catch (IllegalArgumentException ignored) {
            try {
                CraftingBookCategory.valueOf(category);
            } catch (IllegalArgumentException ignored_2) {
                return false;
            }
        }
        return true;
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
        return this.getItemRecipe(ingredientList, type, pattern, cookingTime, name, group, category, result, amount, experience);
    }
}
