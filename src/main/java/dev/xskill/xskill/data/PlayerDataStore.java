package dev.xskill.xskill.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataStore {
    private final Plugin plugin;
    private final File folder;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();

    public PlayerDataStore(Plugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "playerdata");
        if (!folder.exists()) folder.mkdirs();
    }

    public PlayerData get(UUID uuid) {
        return cache.computeIfAbsent(uuid, this::load);
    }

    public void save(PlayerData data) {
        File file = new File(folder, data.uuid().toString() + ".yml");
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("xp", data.xp());
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("xSkill playerdata save failed: " + data.uuid());
        }
    }

    public void flushAll() {
        for (PlayerData data : cache.values()) save(data);
    }

    private PlayerData load(UUID uuid) {
        File file = new File(folder, uuid.toString() + ".yml");
        if (!file.exists()) return new PlayerData(uuid, 0.0);
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        return new PlayerData(uuid, yml.getDouble("xp", 0.0));
    }
}

