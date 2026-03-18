package dev.xskill.xskill.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public final class Keys {
    public static NamespacedKey SWORD_ID;

    private Keys() {
    }

    public static void init(Plugin plugin) {
        SWORD_ID = new NamespacedKey(plugin, "sword_id");
    }
}

