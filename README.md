# RecipesAPI

![Release](https://img.shields.io/github/v/tag/Traqueur-dev/RecipesAPI?label=latest&sort=semver)

**RecipesAPI** is a lightweight and easy-to-use API that allows you to create custom recipes for your Spigot server. Whether you want to add custom shaped or shapeless crafting recipes, furnace smelting recipes, or other custom item interactions, this API makes it simple to do so.

## Features
- **Create Custom Recipes**: Add shaped, shapeless, furnace, and other types of recipes with ease.
- **Recipe Priority System**: Control recipe registration order to handle conflicting recipes (higher priority = registered first).
- **Advanced Recipe Handling**: Support for custom ingredients with metadata (lore, custom model data, persistent data container).
- **Strict Ingredient Matching**: Option to require exact item matches including all metadata for precise recipe control.
- **Easy Integration**: Simple API to integrate into any Spigot plugin.
- **Plugin Hooks**: Built-in support for ItemsAdder and Oraxen items. You can create your own hook with your custom item systems.
- **Version Compatibility**: Works with recent Spigot versions and allows you to create recipes dynamically.
- **Lightweight**: No need to include large libraries or dependencies.
- **Open Source**: Available under the MIT License.
- **Javadoc**: Comprehensive documentation for easy reference.

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
This example demonstrates adding multiple types of recipes including shapeless, shaped, custom ingredients, and furnace recipes.
You can see how easy it is to create and register recipes with the API.
The example plugin is available in the `test-plugin` directory.

### Programmatic Recipe Creation

```java
public final class TestPlugin extends JavaPlugin {

    private RecipesAPI recipesAPI;

    @Override
    public void onEnable() {
        // Initialize RecipesAPI (plugin, debug mode enabled)
        recipesAPI = new RecipesAPI(this, true);

        // 1. Simple shapeless crafting recipe (DIRT -> 64 DIAMOND)
        ItemRecipe recipe1 = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPELESS)
                .setName("example-simple")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .addIngredient(Material.DIRT)
                .build();

        // 2. Shaped crafting recipe (8 DIRT around 1 DIAMOND -> 64 DIAMOND)
        ItemRecipe recipe2 = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPED)
                .setName("example-shaped")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .setPattern("DDD", "DID", "DDD")
                .addIngredient(Material.DIRT, 'D')
                .addIngredient(Material.DIAMOND, 'I')
                .build();

        // 3. Custom ingredient with lore (only lore is checked, displayName can be changed by player)
        ItemStack magicPaper = new ItemStack(Material.PAPER);
        ItemMeta meta = magicPaper.getItemMeta();
        meta.setLore(List.of("§6Magic Paper", "§7Used for special crafting"));
        magicPaper.setItemMeta(meta);

        ItemRecipe recipe3 = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPELESS)
                .setName("example-custom-ingredient")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .addIngredient(magicPaper, false)  // false = normal matching (lore must match)
                .build();

        // 3b. Same recipe but with strict matching (exact item required)
        ItemRecipe recipe3strict = new RecipeBuilder()
                .setType(RecipeType.CRAFTING_SHAPELESS)
                .setName("example-custom-ingredient-strict")
                .setResult(new ItemStack(Material.EMERALD))
                .setAmount(32)
                .addIngredient(magicPaper, true)  // true = strict matching (all metadata must match)
                .setPriority(10)  // Higher priority than normal recipes
                .build();
        
        // 4. Furnace smelting recipe with cooking time and experience
        ItemRecipe recipe4 = new RecipeBuilder()
                .setType(RecipeType.SMELTING)
                .setName("example-furnace")
                .setResult(new ItemStack(Material.DIAMOND))
                .setAmount(64)
                .addIngredient(Material.COAL)
                .setCookingTime(200)  // in ticks (200 ticks = 10 seconds)
                .setExperience(10.0f)
                .build();

        // Add all recipes to the API
        recipesAPI.addRecipe(recipe1);
        recipesAPI.addRecipe(recipe2);
        recipesAPI.addRecipe(recipe3);
        recipesAPI.addRecipe(recipe3strict);
        recipesAPI.addRecipe(recipe4);
    }
}
```

### Loading Recipes from YAML Files

RecipesAPI provides a flexible `RecipeLoader` for loading recipes from YAML files:

```java
public final class TestPlugin extends JavaPlugin {

    private RecipesAPI recipesAPI;
    private RecipeLoader recipeLoader;

    @Override
    public void onEnable() {
        // Initialize RecipesAPI
        recipesAPI = new RecipesAPI(this, true);

        // Create a RecipeLoader and configure it
        recipeLoader = recipesAPI.createLoader()
                .addFolder("recipes/")           // Load all .yml files from recipes/ folder
                .addFolder("recipes/custom/")    // Load from additional folders
                .addFile("special/unique.yml");  // Load a specific file

        // Load all configured recipes
        recipeLoader.load();
    }

    // Reload recipes at runtime
    public void reloadRecipes() {
        recipeLoader.reload();
    }
}
```

**How RecipeLoader works:**
- All paths are relative to the plugin's data folder
- `addFolder()` loads recipes recursively from the specified folder
- If a folder doesn't exist, it automatically extracts default recipes from your plugin JAR
- `addFile()` loads a single recipe file
- `load()` loads all configured recipes
- `reload()` unregisters all recipes and reloads them

## Recipe Types

RecipesAPI supports all vanilla Minecraft recipe types:

- **`CRAFTING_SHAPELESS`** - Shapeless crafting recipes (items in any arrangement)
- **`CRAFTING_SHAPED`** - Shaped crafting recipes (specific pattern required)
- **`SMELTING`** - Furnace smelting recipes
- **`BLASTING`** - Blast furnace recipes
- **`SMOKING`** - Smoker recipes
- **`CAMPFIRE_COOKING`** - Campfire cooking recipes
- **`STONE_CUTTING`** - Stonecutter recipes
- **`SMITHING_TRANSFORM`** - Smithing table transformation recipes

## Custom Ingredients

The API supports several types of ingredients:

- **Material**: Simple material type (e.g., `Material.DIAMOND`)
- **ItemStack**: Items with custom metadata (lore, custom model data, PDC)
- **Strict ItemStack**: Exact item match including all metadata
- **Tag**: Minecraft tags (e.g., planks, logs, wool)
- **Plugin Items**: ItemsAdder and Oraxen custom items

<<<<<<< HEAD
### Ingredient Matching Modes

**Normal Mode** (default):
- Only checks metadata that is present in the recipe ingredient
- Player can add extra metadata without breaking the recipe
- Display name is NOT checked (players can rename items)
- Lore, Custom Model Data, and PDC keys present in ingredient must match

**Strict Mode** (`strict: true`):
- Requires exact match using Bukkit's `ItemStack.isSimilar()`
- All metadata must match exactly
- Use this when you need precise ingredient control

Example in code:
```java
// Normal mode - flexible matching
.addIngredient(customItem, 'C', false)

// Strict mode - exact matching
.addIngredient(customItem, 'C', true)
```

Example in YAML:
```yaml
ingredients:
  - item: item:COBBLESTONE
    sign: 'C'
    strict: true  # Requires exact match
```

### Important Notes
- **Display Name**: Player can rename items - only lore, custom model data, and PDC are checked
- **Strict Mode**: Use `.addIngredient(item, sign, true)` to require exact match including display name

## API Documentation
The API is simple and intuitive to use. You can easily:
- **Define crafting types**: All vanilla recipe types supported
- **Add ingredients**: Regular materials, custom items with `ItemMeta`, or plugin items
- **Set crafting patterns**: For shaped recipes, define the crafting grid with `.setPattern()`
- **Control output**: Set the resulting item and amount
- **Configure cooking**: Set cooking time and experience for smelting recipes

## Plugin Hooks

RecipesAPI provides built-in support for popular custom item plugins:

### Using ItemsAdder Items

```java
// In your YAML recipe file
ingredients:
  - item: itemsadder:custom_item_id

# Or in code
ItemRecipe recipe = new RecipeBuilder()
    .setType(RecipeType.CRAFTING_SHAPELESS)
    .setName("itemsadder-recipe")
    .setResult(itemsAdderItem)  // Get from ItemsAdder API
    .addIngredient(/* ItemsAdder ingredient */)
    .build();
```

### Using Oraxen Items

```java
// In your YAML recipe file
ingredients:
  - item: oraxen:custom_item_id

# Or in code
ItemRecipe recipe = new RecipeBuilder()
    .setType(RecipeType.CRAFTING_SHAPELESS)
    .setName("oraxen-recipe")
    .setResult(oraxenItem)  // Get from Oraxen API
    .addIngredient(/* Oraxen ingredient */)
    .build();
```

### Creating Custom Hooks

You can create your own hooks for any custom item plugin:

```java
public class MyCustomItemHook implements Hook {

    @Override
    public String getPluginName() {
        return "MyCustomPlugin";
    }

    @Override
    public Ingredient getIngredient(String data, Character sign) {
        // Create your custom ingredient implementation
        return new MyCustomIngredient(data, sign);
    }

    @Override
    public ItemStack getItemStack(String data) {
        // Return the ItemStack for your custom item
        return MyCustomPlugin.getItem(data);
    }
}

// Register your hook
Hook.addHook(new MyCustomItemHook());
```

## YAML Configuration

RecipesAPI supports loading recipes from YAML files. Simply place `.yml` files in your plugin's `recipes/` folder (or any folder you configure with `RecipeLoader`).

### Recipe File Format

```yaml
type: CRAFTING_SHAPED
pattern:
  - "DDD"
  - "DID"
  - "DDD"
ingredients:
  - item: DIRT
    sign: D
  - item: DIAMOND
    sign: I
result:
  item: DIAMOND
  amount: 64
group: "custom_recipes"
category: "MISC"
```

### YAML Recipe Fields

#### Required Fields
- `type` - Recipe type (see Recipe Types section)
- `ingredients` - List of ingredients (see Ingredient Types below)
- `result.item` - The resulting item

#### Optional Fields
- `result.amount` - Output amount (default: 1)
- `pattern` - Pattern for shaped recipes (max 3 rows, max 3 chars per row)
- `group` - Recipe group for the recipe book
- `category` - Recipe category (BUILDING, REDSTONE, EQUIPMENT, MISC for crafting; FOOD, BLOCKS, MISC for cooking)
- `cooking-time` - Cooking time in ticks for smelting recipes (default: 0)
- `experience` - Experience reward for smelting recipes (default: 0.0)
- `priority` - Recipe registration priority (default: 0, higher = registered first)

### Pattern Validation

For `CRAFTING_SHAPED` recipes, the pattern is validated:
- Maximum 3 rows
- Maximum 3 characters per row
- All pattern characters must have corresponding ingredients with matching signs
- Empty rows are not allowed

### Ingredient Types in YAML

#### Basic Format
```yaml
ingredients:
  - item: <type>:<value>
    sign: 'X'      # Optional: Required for shaped recipes
    strict: true   # Optional: Enable strict matching (default: false)
```

#### Supported Types

- **Simple Material** (auto-detected):
  ```yaml
  - item: DIAMOND  # No prefix = Material
  ```

- **Explicit Material** (same as above):
  ```yaml
  - item: material:DIAMOND
  ```

- **ItemStack from Material** (with metadata support):
  ```yaml
  - item: item:COBBLESTONE
    strict: true  # Recommended for items with metadata
  ```
  Creates an ItemStack that can have metadata (lore, custom model data, PDC)

- **ItemStack from Base64**:
  ```yaml
  - item: base64:BASE64_ENCODED_ITEM_STRING
    strict: true  # Recommended for custom items
  ```
  Load a custom item from a serialized Base64 string

- **Minecraft Tag**:
  ```yaml
  - item: tag:planks  # Accepts any plank type
  ```

- **ItemsAdder Item**:
  ```yaml
  - item: itemsadder:custom_item_id
  ```

- **Oraxen Item**:
  ```yaml
  - item: oraxen:custom_item_id
  ```

- **Custom Plugin Hook**:
  ```yaml
  - item: yourplugin:custom_item_id
  ```

#### Field Details
- `item: MATERIAL_NAME` - Simple material
- `item: material:MATERIAL_NAME` - Explicit material
- `item: tag:TAG_NAME` - Minecraft tag
- `item: item:BASE64_STRING` or `item: base64:BASE64_STRING` - Custom item from Base64
- `item: itemsadder:ITEM_ID` - ItemsAdder item
- `item: oraxen:ITEM_ID` - Oraxen item
- `sign: X` - Character used in shaped recipe patterns (required for shaped recipes)
- `strict: true` - Require exact item match including display name (optional, default: false)

### Example: Smelting Recipe

```yaml
type: SMELTING
ingredients:
  - item: COAL
result:
  item: DIAMOND
  amount: 64
cooking-time: 200
experience: 10.0
category: MISC
```

### Example: Shapeless Recipe with Custom Item

```yaml
type: CRAFTING_SHAPELESS
ingredients:
  - item: base64:BASE64_ENCODED_ITEM_HERE
    strict: true
result:
  item: DIAMOND
  amount: 1
```

## Recipe Priority System

When multiple recipes have similar ingredients, the **priority** field determines which recipe is checked first. This is crucial for handling conflicting recipes.

### How Priority Works
- Recipes are sorted by priority before registration (higher priority = registered first)
- Default priority is `0`
- Higher priority recipes are checked before lower priority ones
- Useful for specific recipes that should take precedence over generic ones

### Example Use Case: Compressed Cobblestone

```yaml
# cobblestone_to_compressed_x1.yml
type: CRAFTING_SHAPED
priority: 0  # Lower priority (default)
pattern:
  - "CCC"
  - "CCC"
  - "CCC"
ingredients:
  - item: material:COBBLESTONE
    sign: 'C'
result:
  item: yourplugin:compressed_cobblestone_x1
  amount: 1
```

```yaml
# compressed_x1_to_x2.yml
type: CRAFTING_SHAPED
priority: 10  # Higher priority - checked first!
pattern:
  - "CCC"
  - "CCC"
  - "CCC"
ingredients:
  - item: item:COMPRESSED_COBBLESTONE_X1  # Custom item with metadata
    sign: 'C'
    strict: true  # Important: ensures exact match
result:
  item: yourplugin:compressed_cobblestone_x2
  amount: 1
```

In this example, the x1→x2 recipe will be checked first because it has `priority: 10`. The `strict: true` flag ensures that only compressed x1 cobblestone (not regular cobblestone) triggers this recipe.

### Priority in Code

```java
ItemRecipe highPriorityRecipe = new RecipeBuilder()
    .setType(RecipeType.CRAFTING_SHAPED)
    .setName("specific-recipe")
    .setPriority(10)  // Higher priority
    .setPattern("AAA", "AAA", "AAA")
    .addIngredient(specificItem, 'A', true)  // Strict matching
    .setResult(new ItemStack(Material.DIAMOND))
    .build();

ItemRecipe lowPriorityRecipe = new RecipeBuilder()
    .setType(RecipeType.CRAFTING_SHAPED)
    .setName("generic-recipe")
    .setPriority(0)  // Default priority
    .setPattern("AAA", "AAA", "AAA")
    .addIngredient(Material.STONE, 'A')
    .setResult(new ItemStack(Material.COAL))
    .build();
```

## Parsing Ingredients Programmatically

RecipesAPI provides a public utility method to parse ingredients from strings, useful for loading recipes from custom sources or configuration files.

### Using Util.parseIngredient()

```java
import fr.traqueur.recipes.api.Util;
import fr.traqueur.recipes.api.domains.Ingredient;

// Parse a simple material
Ingredient diamond = Util.parseIngredient("DIAMOND");

// Parse with a sign for shaped recipes
Ingredient stone = Util.parseIngredient("item:STONE", 'S');

// Parse with strict mode enabled
Ingredient customItem = Util.parseIngredient("item:COBBLESTONE", 'C', true);

// Parse from base64
Ingredient base64Item = Util.parseIngredient("base64:YOUR_BASE64_STRING", null, true);

// Parse from tags
Ingredient planks = Util.parseIngredient("tag:planks", 'P');

// Parse from plugin items
Ingredient iaItem = Util.parseIngredient("itemsadder:custom_item", 'I');
Ingredient oraxenItem = Util.parseIngredient("oraxen:custom_sword", 'S');
```

### Method Signatures

```java
// Full control
public static Ingredient parseIngredient(String itemString, Character sign, boolean strict)

// Without strict mode (default: false)
public static Ingredient parseIngredient(String itemString, Character sign)

// Shapeless recipe (no sign)
public static Ingredient parseIngredient(String itemString)
```

### Supported Formats
All formats supported in YAML are also supported here:
- `"MATERIAL_NAME"` → MaterialIngredient
- `"material:MATERIAL_NAME"` → MaterialIngredient
- `"item:MATERIAL_NAME"` → ItemStackIngredient (supports metadata)
- `"base64:BASE64_STRING"` → ItemStackIngredient from serialized item
- `"tag:TAG_NAME"` → TagIngredient
- `"pluginname:item_id"` → Custom plugin hook

## Resources

- **Javadoc**: [API Documentation](https://jitpack.io/com/github/Traqueur-dev/RecipesAPI/latest/javadoc/)
- **Wiki**: [GitHub Wiki](https://github.com/Traqueur-dev/RecipesAPI/wiki)
- **Issues**: [Report bugs or request features](https://github.com/Traqueur-dev/RecipesAPI/issues)

## License
This project is licensed under the MIT License.
