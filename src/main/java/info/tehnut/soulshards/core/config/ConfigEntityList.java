package info.tehnut.soulshards.core.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.Set;

public class ConfigEntityList {
    private static final Set<String> DEFAULT_DISABLES = Sets.newHashSet(
            "minecraft:armor_stand",
            "minecraft:elder_guardian",
            "minecraft:ender_dragon",
            "minecraft:wither",
            "minecraft:wither",
            "minecraft:player"
    );

    private Map<String, Boolean> entities;

    public ConfigEntityList(Map<String, Boolean> entities) {
        this.entities = entities;
    }

    public ConfigEntityList() {
        this(getDefaults());
    }

    public boolean isEnabled(Identifier entityId) {
        return entities.getOrDefault(entityId.toString(), false);
    }

    private static Map<String, Boolean> getDefaults() {
        Map<String, Boolean> defaults = Maps.newHashMap();
        Registry.ENTITY_TYPE.stream()
                .filter(e -> e.getCategory() != EntityCategory.MISC)
                .forEach(e -> {
                    String entityId = Registry.ENTITY_TYPE.getId(e).toString();
                    defaults.put(entityId, !DEFAULT_DISABLES.contains(entityId));
                });
        return defaults;
    }
}
