package fr.traqueur.recipes.api;

import org.bukkit.Material;
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

}
