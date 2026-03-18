package dev.xskill.xskill.placeholders;

import dev.xskill.xskill.XSkillPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.Locale;

public final class XSkillPlaceholders extends PlaceholderExpansion {
    private final XSkillPlugin plugin;

    public XSkillPlaceholders(XSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "xskill";
    }

    @Override
    public String getAuthor() {
        return "xrefunsen";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || !player.isOnline()) return "";
        var p = player.getPlayer();
        if (p == null) return "";
        String k = params == null ? "" : params.toLowerCase(Locale.ROOT);
        return switch (k) {
            case "level" -> String.valueOf(plugin.levels().level(p));
            case "xp" -> String.format(Locale.ROOT, "%.2f", plugin.levels().xp(p));
            case "xp_to_next" -> String.format(Locale.ROOT, "%.2f", plugin.levels().xpToNext(p));
            default -> "";
        };
    }
}

