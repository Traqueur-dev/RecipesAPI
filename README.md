# RecipesAPI

![Release](https://img.shields.io/github/v/tag/Traqueur-dev/RecipesAPI?label=latest&sort=semver)

**RecipesAPI** is a lightweight and easy-to-use API that allows you to create custom recipes for your Spigot server. Whether you want to add custom shaped or shapeless crafting recipes, furnace smelting recipes, or other custom item interactions, this API makes it simple to do so.

## Features
- **Create Custom Recipes**: Add shaped, shapeless, and furnace, and other type recipes with ease.
- **Advanced Recipe Handling**: Support for custom ingredients with meta data (e.g., items with custom names).
- **Easy Integration**: Simple API to integrate into any Spigot plugin.
- **Version Compatibility**: Works with recent Spigot versions and allows you to create recipes dynamically. Folia compatibility if you use FoliaLib.
## Installation

0. **Prerequisites**: 
   - Make sure you have **Java 21+** and **Spigot** installed on your system.
1. Add **RecipesAPI** to your project via **JitPack**. Add the following to your `build.gradle`:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Traqueur-dev:RecipesAPI:VERSION'
}
```

2. Since you are using **Shadow Plugin** for building, make sure to relocate the API to avoid conflicts:

```groovy
shadowJar {
    relocate 'fr.traqueur.recipes', 'your.plugin.package.recipes'
}
```

3. Build your plugin with `./gradlew build`.

## Usage Example

Below is an example of how to use **RecipesAPI** in your Spigot plugin. 
This example demonstrates adding four types of recipes: a simple shapeless crafting recipe, a shaped crafting recipe, a custom ingredient shapeless recipe, and a furnace recipe.
You can see how easy it is to create and register recipes with the API.
The exemple plugin is available in the `test-plugin` directory.

```java
public final class TestPlugin extends JavaPlugin {

    private RecipesAPI recipesAPI;

    @Override
    public void onEnable() {
        // Initialize RecipesAPI
        recipesAPI = new RecipesAPI(this, true);

        // Create a simple shapeless crafting recipe (DIRT -> 64 DIAMOND)
        ItemRecipe recipe = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPELESS)
                .setName("example-simple")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .addIngredient(Material.DIRT)
                .build();

        // Create a shaped crafting recipe (DIRT and DIAMOND -> 64 DIAMOND)
        ItemRecipe recipe2 = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPED)
                .setName("example-shaped")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .setPattern("DDD", "DID", "DDD")
                .addIngredient(Material.DIRT, 'D')
                .addIngredient(Material.DIAMOND, 'I')
                .build();

        // Create a shapeless recipe with a custom ingredient (named PAPER)
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

        // Create a furnace smelting recipe (PAPER -> 64 DIAMOND)
        ItemRecipe recipe4 = new RecipeBuilder()
                .setType(RecipeType.SMELTING)
                .setName("example-furnace")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .addIngredient(ingredient)
                .setCookingTime(10)
                .build();

        // Add the recipes to the API
        recipesAPI.addRecipe(recipe);
        recipesAPI.addRecipe(recipe2);
        recipesAPI.addRecipe(recipe3);
        recipesAPI.addRecipe(recipe4);

        // Register the recipes with the server
        recipesAPI.registerRecipes();
    }
}
```

## How to Use

- **Shapeless Recipe**: Add items to crafting in any arrangement.
- **Shaped Recipe**: Define specific patterns for crafting items.
- **Custom Ingredients**: Use items with custom names or metadata in recipes.
- **Furnace Recipes**: Create custom smelting recipes with adjustable cooking time.

## API Documentation
The API is simple and intuitive to use. You can easily:
- **Define crafting types**: `RecipeType.CRAFTING_SHAPELESS`, `RecipeType.CRAFTING_SHAPED`,
`RecipeType.SMELTING`, etc.
- **Add ingredients**: Either regular materials or custom items with `ItemMeta`.
- **Set crafting patterns**: For shaped recipes, you can define the crafting grid with `.setPattern()`.
- **Control output**: Set the resulting item and amount.

You can check javadoc here : [Javadoc](https://javadoc.jitpack.io/com/github/Traqueur-dev/RecipesAPI/1.1.0/javadoc/)

## License
This project is licensed under the MIT License.
