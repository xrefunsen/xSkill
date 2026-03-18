package dev.xskill.xskill.model;

import org.bukkit.configuration.ConfigurationSection;

public record SwordAbilityConfig(
        String id,
        boolean enabled,
        AbilityTrigger trigger,
        ConfigurationSection section
) {
}

