package dev.xskill.xskill.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private Text() {
    }

    public static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> colorList(List<String> list) {
        List<String> out = new ArrayList<>();
        if (list == null) return out;
        for (String s : list) out.add(color(s));
        return out;
    }

    public static Component component(String s) {
        if (s == null) return Component.empty();
        return LEGACY.deserialize(color(s));
    }
}

