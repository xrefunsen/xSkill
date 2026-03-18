package dev.xskill.xskill.services;

import dev.xskill.xskill.model.AbilityTrigger;
import dev.xskill.xskill.model.SwordAbilityConfig;
import dev.xskill.xskill.model.SwordDefinition;
import dev.xskill.xskill.util.Text;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SwordRegistry {
    private final Plugin plugin;
    private Map<String, SwordDefinition> swords = new HashMap<>();

    public SwordRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("swords");
        Map<String, SwordDefinition> next = new HashMap<>();
        if (root != null) {
            for (String id : root.getKeys(false)) {
                ConfigurationSection s = root.getConfigurationSection(id);
                if (s == null) continue;
                boolean enabled = s.getBoolean("enabled", true);
                Material material = Material.matchMaterial(s.getString("material", "DIAMOND_SWORD"));
                if (material == null) material = Material.DIAMOND_SWORD;
                String name = Text.color(s.getString("name", id));
                java.util.List<String> lore = Text.colorList(s.getStringList("lore"));
                int cmd = s.getInt("customModelData", 0);
                int minLevel = Optional.ofNullable(s.getConfigurationSection("requirements"))
                        .map(r -> r.getInt("minLevel", 1))
                        .orElse(1);
                ConfigurationSection combat = s.getConfigurationSection("combat");
                double dmg = combat != null ? combat.getDouble("damageBonus", 0.0) : 0.0;

                Map<String, SwordAbilityConfig> abilities = new HashMap<>();
                ConfigurationSection abs = s.getConfigurationSection("abilities");
                if (abs != null) {
                    for (String aid : abs.getKeys(false)) {
                        ConfigurationSection a = abs.getConfigurationSection(aid);
                        if (a == null) continue;
                        boolean aEnabled = a.getBoolean("enabled", true);
                        AbilityTrigger trigger = parseTrigger(a.getString("trigger", "SHIFT_RIGHT_CLICK"));
                        abilities.put(aid, new SwordAbilityConfig(aid, aEnabled, trigger, a));
                    }
                }

                next.put(id, new SwordDefinition(id, enabled, material, name, lore, cmd, minLevel, dmg, combat, abilities));
            }
        }
        this.swords = next;
    }

    public Map<String, SwordDefinition> all() {
        return Collections.unmodifiableMap(swords);
    }

    public SwordDefinition get(String id) {
        return swords.get(id);
    }

    private AbilityTrigger parseTrigger(String raw) {
        if (raw == null) return AbilityTrigger.SHIFT_RIGHT_CLICK;
        try {
            return AbilityTrigger.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return AbilityTrigger.SHIFT_RIGHT_CLICK;
        }
    }
}

