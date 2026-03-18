package dev.xskill.xskill.commands;

import dev.xskill.xskill.XSkillPlugin;
import dev.xskill.xskill.items.SwordItems;
import dev.xskill.xskill.model.SwordDefinition;
import dev.xskill.xskill.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class XSkillCommand implements CommandExecutor, TabCompleter {
    private final XSkillPlugin plugin;

    public XSkillCommand(XSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Text.color("&7/xskill give <player> <swordId> [amount]"));
            sender.sendMessage(Text.color("&7/xskill reload"));
            sender.sendMessage(Text.color("&7/xskill stats [player]"));
            sender.sendMessage(Text.color("&7/xskill level [player]"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "reload" -> reload(sender);
            case "give" -> give(sender, args);
            case "stats" -> stats(sender, args);
            case "level" -> level(sender, args);
            default -> {
                sender.sendMessage(Text.color("&cBilinmeyen komut."));
                yield true;
            }
        };
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("xskill.reload") && !sender.hasPermission("xskill.admin")) {
            sender.sendMessage(Text.color("&cYetkin yok."));
            return true;
        }
        plugin.swords().reload();
        sender.sendMessage(Text.color("&aYenilendi."));
        return true;
    }

    private boolean give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("xskill.give") && !sender.hasPermission("xskill.admin")) {
            sender.sendMessage(Text.color("&cYetkin yok."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Text.color("&cKullanım: /xskill give <player> <swordId> [amount]"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&cOyuncu bulunamadı."));
            return true;
        }
        String swordId = args[2];
        SwordDefinition def = plugin.swords().get(swordId);
        if (def == null || !def.enabled()) {
            sender.sendMessage(Text.color("&cKılıç bulunamadı/kapalı."));
            return true;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Integer.parseInt(args[3]));
            } catch (Exception ignored) {
                amount = 1;
            }
        }
        for (int i = 0; i < amount; i++) {
            var item = SwordItems.create(def);
            item.setAmount(1);
            target.getInventory().addItem(item);
        }
        sender.sendMessage(Text.color("&aVerildi: &f" + swordId + " &7x" + amount));
        return true;
    }

    private boolean stats(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("xskill.stats.others") && !sender.hasPermission("xskill.admin")) {
                sender.sendMessage(Text.color("&cYetkin yok."));
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Text.color("&cOyuncu bulunamadı."));
                return true;
            }
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Text.color("&cKonsol için: /xskill stats <player>"));
                return true;
            }
            target = p;
        }
        int level = plugin.levels().level(target);
        double xp = plugin.levels().xp(target);
        double into = plugin.levels().xpIntoLevel(target);
        double toNext = plugin.levels().xpToNext(target);
        sender.sendMessage(Text.color("&6xSkill &7- &f" + target.getName()));
        sender.sendMessage(Text.color("&7Seviye: &a" + level));
        sender.sendMessage(Text.color("&7Toplam XP: &b" + String.format(Locale.ROOT, "%.2f", xp)));
        if (toNext > 0) {
            sender.sendMessage(Text.color("&7Seviye içi XP: &b" + String.format(Locale.ROOT, "%.2f", into) + "&7/&b" + String.format(Locale.ROOT, "%.2f", toNext)));
        } else {
            sender.sendMessage(Text.color("&7Maks seviye."));
        }
        return true;
    }

    private boolean level(CommandSender sender, String[] args) {
        if (args.length >= 2) return stats(sender, new String[]{"stats", args[1]});
        return stats(sender, new String[]{"stats"});
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("give", "reload", "stats", "level"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            return filter(plugin.swords().all().entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue().enabled())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()), args[2]);
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("stats") || args[0].equalsIgnoreCase("level"))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }
        return List.of();
    }

    private List<String> filter(List<String> in, String token) {
        if (token == null || token.isBlank()) return new ArrayList<>(in);
        String t = token.toLowerCase(Locale.ROOT);
        return in.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(t)).toList();
    }
}

