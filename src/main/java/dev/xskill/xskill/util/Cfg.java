package dev.xskill.xskill.util;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

public final class Cfg {
    private Cfg() {
    }

    public static int i(ConfigurationSection s, String path, int def) {
        return s != null ? s.getInt(path, def) : def;
    }

    public static double d(ConfigurationSection s, String path, double def) {
        return s != null ? s.getDouble(path, def) : def;
    }

    public static boolean b(ConfigurationSection s, String path, boolean def) {
        return s != null ? s.getBoolean(path, def) : def;
    }

    public static String str(ConfigurationSection s, String path, String def) {
        if (s == null) return def;
        String v = s.getString(path);
        return v != null ? v : def;
    }

    public static Sound sound(ConfigurationSection s, String path, Sound def) {
        String raw = str(s, path, null);
        if (raw == null) return def;
        try {
            return Sound.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return def;
        }
    }

    public static Particle particle(ConfigurationSection s, String path, Particle def) {
        String raw = str(s, path, null);
        if (raw == null) return def;
        try {
            return Particle.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return def;
        }
    }

    public static Material material(ConfigurationSection s, String path, Material def) {
        String raw = str(s, path, null);
        if (raw == null) return def;
        Material m = Material.matchMaterial(raw.trim());
        return m != null ? m : def;
    }

    public static PotionEffectType potion(ConfigurationSection s, String path, PotionEffectType def) {
        String raw = str(s, path, null);
        if (raw == null) return def;
        PotionEffectType p = PotionEffectType.getByName(raw.trim().toUpperCase());
        return p != null ? p : def;
    }
}

