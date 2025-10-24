package fr.traqueur.recipes.api;

import fr.traqueur.recipes.api.domains.Ingredient;
import fr.traqueur.recipes.api.hook.Hook;
import fr.traqueur.recipes.impl.domains.ingredients.ItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.MaterialIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.StrictItemStackIngredient;
import fr.traqueur.recipes.impl.domains.ingredients.TagIngredient;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

public class Util {

    /**
     * This method is used to get the itemstack from base64 string
     * @param base64itemstack the base64 item stack.
     * @return the item stack.
     */
    public static ItemStack getItemStack(String base64itemstack) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(base64itemstack));
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            ObjectInputStream objectInputStream = new BukkitObjectInputStream(gzipInputStream);
            Object deserialized = objectInputStream.readObject();
            objectInputStream.close();

            if (!(deserialized instanceof ItemStack)) {
                throw new IllegalArgumentException("The deserialized object is not an ItemStack.");
            }

            return (ItemStack) deserialized;
        } catch (IOException exception) {
            throw new IllegalArgumentException("The itemstack " + base64itemstack + " is not a valid base64 or corrupted: " + exception.getMessage());
        } catch (ClassNotFoundException exception) {
            throw new IllegalArgumentException("The itemstack " + base64itemstack + " contains an unknown class: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("The itemstack " + base64itemstack + " is not valid: " + exception.getMessage());
        }
    }

    public static String fromItemStack(ItemStack itemStack) {
        try {
            java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
            java.util.zip.GZIPOutputStream gzipOutputStream = new java.util.zip.GZIPOutputStream(byteArrayOutputStream);
            org.bukkit.util.io.BukkitObjectOutputStream bukkitObjectOutputStream = new org.bukkit.util.io.BukkitObjectOutputStream(gzipOutputStream);
            bukkitObjectOutputStream.writeObject(itemStack);
            bukkitObjectOutputStream.close();
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalArgumentException("The itemstack " + itemStack + " cannot be serialized: " + e.getMessage());
        }
    }

    /**
     * This method is used to get the material from the string.
     * @param material the material string.
     * @return the material.
     */
    public static Material getMaterial(String material) {
        try {
            return Material.valueOf(material.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("The material " + material + " isn't valid.");
        }
    }

    /**
     * Parse an ingredient from a string.
     * @param itemString The string representation of the ingredient (e.g., "COBBLESTONE", "material:STONE", "item:DIAMOND", "base64:xxx", "tag:planks", "plugin:custom_item")
     * @param sign The sign of the ingredient (can be null for shapeless recipes)
     * @param strict Whether the ingredient should use strict matching (only applies to item: and base64: types)
     * @return The parsed ingredient
     */
    public static Ingredient parseIngredient(String itemString, Character sign, boolean strict) {
        String[] data = itemString.split(":", 2);
        if(data.length == 1) {
            return new MaterialIngredient(getMaterial(data[0]), sign);
        } else {
            return switch (data[0]) {
                case "material" -> new MaterialIngredient(getMaterial(data[1]), sign);
                case "tag" -> new TagIngredient(getTag(data[1]), sign);
                case "item" -> {
                    // Create ItemStack from Material for ItemStackIngredient
                    ItemStack stack = new ItemStack(getMaterial(data[1]));
                    if(strict) {
                        yield new StrictItemStackIngredient(stack, sign);
                    }
                    yield new ItemStackIngredient(stack, sign);
                }
                case "base64" -> {
                    if(strict) {
                        yield new StrictItemStackIngredient(getItemStack(data[1]), sign);
                    }
                    yield new ItemStackIngredient(getItemStack(data[1]), sign);
                }
                default -> Hook.HOOKS.stream()
                        .filter(Hook::isEnable)
                        .filter(hook -> hook.getPluginName().equalsIgnoreCase(data[0]))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("The data " + data[0] + " isn't valid."))
                        .getIngredient(data[1], sign);
            };
        }
    }

    /**
     * Parse an ingredient from a string without strict mode.
     * @param itemString The string representation of the ingredient
     * @param sign The sign of the ingredient (can be null for shapeless recipes)
     * @return The parsed ingredient
     */
    public static Ingredient parseIngredient(String itemString, Character sign) {
        return parseIngredient(itemString, sign, false);
    }

    /**
     * Parse an ingredient from a string without sign and strict mode.
     * @param itemString The string representation of the ingredient
     * @return The parsed ingredient
     */
    public static Ingredient parseIngredient(String itemString) {
        return parseIngredient(itemString, null, false);
    }

    /**
     * This method is used to get Tag from the string.
     * @param data the data to get the tag.
     * @return the tag.
     */
    private static Tag<Material> getTag(String data) {
        Tag<Material> blockTag = Bukkit.getTag(Tag.REGISTRY_BLOCKS, NamespacedKey.minecraft(data), Material.class);
        if (blockTag != null) {
            return blockTag;
        }
        Tag<Material> itemTag = Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.minecraft(data), Material.class);
        if (itemTag != null) {
            return itemTag;
        }
        throw new IllegalArgumentException("The tag " + data + " isn't valid.");
    }

}
