package fr.traqueur.testplugin;

import fr.traqueur.recipes.api.RecipeType;
import fr.traqueur.recipes.api.RecipesAPI;
import fr.traqueur.recipes.impl.RecipeBuilder;
import fr.traqueur.recipes.impl.domains.ItemRecipe;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public final class TestPlugin extends JavaPlugin {

    private RecipesAPI recipesAPI;

    @Override
    public void onEnable() {
        recipesAPI = new RecipesAPI(this, true);

        ItemRecipe recipe = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPELESS)
                .setName("example-simple")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .addIngredient(Material.DIRT)
                .build();

        ItemRecipe recipe2 = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPED)
                .setName("example-shaped")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .setPattern("DDD", "DID", "DDD")
                .addIngredient(Material.DIRT, 'D')
                .addIngredient(Material.DIAMOND, 'I')
                .build();

        ItemStack ingredient = new ItemStack(Material.PAPER);
        ItemMeta meta = ingredient.getItemMeta();
        meta.setDisplayName("Dirt Magic");
        ingredient.setItemMeta(meta);

        ItemRecipe recipe3 = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPELESS)
                .setName("example-complex")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .addIngredient(ingredient)
                .build();

        ItemRecipe recipe4 = new RecipeBuilder()
                .setType(RecipeType.SMELTING)
                .setName("example-furnace")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .addIngredient(ingredient)
                .setCookingTime(10)
                .build();

        recipesAPI.addRecipe(recipe);
        recipesAPI.addRecipe(recipe2);
        recipesAPI.addRecipe(recipe3);
        recipesAPI.addRecipe(recipe4);

        recipesAPI.registerRecipes();
    }

    @Override
    public void onDisable() {
       recipesAPI.unregisterRecipes();
    }
}
