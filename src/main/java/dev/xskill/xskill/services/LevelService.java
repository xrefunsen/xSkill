package dev.xskill.xskill.services;

import dev.xskill.xskill.data.PlayerData;
import dev.xskill.xskill.data.PlayerDataStore;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class LevelService {
    private final Plugin plugin;
    private final PlayerDataStore store;

    public LevelService(Plugin plugin, PlayerDataStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    public double xp(Player player) {
        return store.get(player.getUniqueId()).xp();
    }

    public void addXp(Player player, double amount) {
        if (amount <= 0) return;
        if (!plugin.getConfig().getBoolean("leveling.enabled", true)) return;
        PlayerData data = store.get(player.getUniqueId());
        data.setXp(data.xp() + amount);
    }

    public int level(Player player) {
        if (!plugin.getConfig().getBoolean("leveling.enabled", true)) return 1;
        int max = Math.max(1, plugin.getConfig().getInt("leveling.maxLevel", 100));
        double xp = xp(player);
        int level = 1;
        double remaining = xp;
        while (level < max) {
            double need = xpToNext(level);
            if (remaining < need) break;
            remaining -= need;
            level++;
        }
        return level;
    }

    public double xpIntoLevel(Player player) {
        if (!plugin.getConfig().getBoolean("leveling.enabled", true)) return 0.0;
        int max = Math.max(1, plugin.getConfig().getInt("leveling.maxLevel", 100));
        double xp = xp(player);
        int level = 1;
        double remaining = xp;
        while (level < max) {
            double need = xpToNext(level);
            if (remaining < need) break;
            remaining -= need;
            level++;
        }
        return remaining;
    }

    public double xpToNext(Player player) {
        if (!plugin.getConfig().getBoolean("leveling.enabled", true)) return 0.0;
        int level = level(player);
        int max = Math.max(1, plugin.getConfig().getInt("leveling.maxLevel", 100));
        if (level >= max) return 0.0;
        return xpToNext(level);
    }

    public double scale(Player player, String key, double base) {
        int lvl = level(player);
        double perLevel = plugin.getConfig().getDouble("leveling.scaling." + key + ".perLevel", 0.0);
        double min = plugin.getConfig().getDouble("leveling.scaling." + key + ".min", Double.NEGATIVE_INFINITY);
        double max = plugin.getConfig().getDouble("leveling.scaling." + key + ".max", Double.POSITIVE_INFINITY);
        double v = base + perLevel * Math.max(0, lvl - 1);
        if (v < min) v = min;
        if (v > max) v = max;
        return v;
    }

    private double xpToNext(int level) {
        double base = plugin.getConfig().getDouble("leveling.xp.baseToNext", 100.0);
        double add = plugin.getConfig().getDouble("leveling.xp.perLevelAdd", 25.0);
        return Math.max(1.0, base + add * (level - 1));
    }
}

