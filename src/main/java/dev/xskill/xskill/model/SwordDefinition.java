package dev.xskill.xskill.model;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

public record SwordDefinition(
        String id,
        boolean enabled,
        Material material,
        String name,
        java.util.List<String> lore,
        int customModelData,
        int minLevel,
        double damageBonus,
        ConfigurationSection combatSection,
        Map<String, SwordAbilityConfig> abilities
) {
}

