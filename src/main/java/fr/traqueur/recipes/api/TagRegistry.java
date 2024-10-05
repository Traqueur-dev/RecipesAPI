package fr.traqueur.recipes.api;

import org.bukkit.Material;
import org.bukkit.Tag;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TagRegistry {

    private static final Map<String, Tag<Material>> tagMap = new HashMap<>();

    static {
        for (Field field : Tag.class.getDeclaredFields()) {
            if (Tag.class.isAssignableFrom(field.getType())) {
                try {
                    Class<?> genericType = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    if (Material.class.isAssignableFrom(genericType)) {
                        register(field.getName(), (Tag<Material>) field.get(null));
                    }
                } catch (Exception exception) {
                    throw new RuntimeException("Failed to register tag: " + field.getName(), exception);
                }
            }
        }
    }

    private static void register(String key, Tag<Material> tag) {
        tagMap.put(key, tag);
    }

    public static Optional<Tag<Material>> getTag(String key) {
        return Optional.ofNullable(tagMap.get(key.toUpperCase()));
    }
}