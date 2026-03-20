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
    private static final int GIVE_MAX_AMOUNT = 1024;

    private final XSkillPlugin plugin;

    public XSkillCommand(XSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Text.color("&7/xskill give <id> &8- &7apply full definition to held item (any item)"));
            sender.sendMessage(Text.color("&7/xskill set <id> &8- &7set definition ID only on held item"));
            sender.sendMessage(Text.color("&7/xskill give <player> <id> [amount]"));
            sender.sendMessage(Text.color("&7/xskill reload"));
            sender.sendMessage(Text.color("&7/xskill stats [player]"));
            sender.sendMessage(Text.color("&7/xskill level [player]"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "reload" -> reload(sender);
            case "give" -> give(sender, args);
            case "set" -> set(sender, args);
            case "stats" -> stats(sender, args);
            case "level" -> level(sender, args);
            default -> {
                sender.sendMessage(Text.color("&cUnknown command."));
                yield true;
            }
        };
    }

    private boolean reload(CommandSender sender) {
        if (!sender.hasPermission("xskill.reload") && !sender.hasPermission("xskill.admin")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        plugin.swords().reload();
        sender.sendMessage(Text.color("&aReloaded."));
        return true;
    }

    private boolean give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("xskill.give") && !sender.hasPermission("xskill.admin")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (args.length == 2) {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Text.color("&cConsole: /xskill give <player> <id> [amount]"));
                return true;
            }
            String swordId = args[1];
            if (swordId.isBlank()) {
                sender.sendMessage(Text.color("&cInvalid id."));
                return true;
            }
            SwordDefinition def = plugin.swords().get(swordId);
            if (def == null || !def.enabled()) {
                sender.sendMessage(Text.color("&cUnknown or disabled definition."));
                return true;
            }
            var hand = p.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) {
                sender.sendMessage(Text.color("&cHold any item in your main hand."));
                return true;
            }
            if (!SwordItems.applyFull(hand, def)) {
                sender.sendMessage(Text.color("&cCould not apply to item."));
                return true;
            }
            sender.sendMessage(Text.color("&aApplied definition &f" + swordId + " &ato your held item."));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Text.color("&cUsage: /xskill give <id> &8| &7/xskill give <player> <id> [amount]"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Text.color("&cPlayer not found."));
            return true;
        }
        String swordId = args[2];
        if (swordId.isBlank()) {
            sender.sendMessage(Text.color("&cInvalid id."));
            return true;
        }
        SwordDefinition def = plugin.swords().get(swordId);
        if (def == null || !def.enabled()) {
            sender.sendMessage(Text.color("&cUnknown or disabled definition."));
            return true;
        }
        int amount = 1;
        if (args.length >= 4) {
            try {
                long parsed = Long.parseLong(args[3]);
                amount = (int) Math.min(GIVE_MAX_AMOUNT, Math.max(1L, parsed));
            } catch (Exception ignored) {
                amount = 1;
            }
        }
        for (int i = 0; i < amount; i++) {
            var item = SwordItems.create(def);
            item.setAmount(1);
            target.getInventory().addItem(item);
        }
        sender.sendMessage(Text.color("&aGiven: &f" + swordId + " &7x" + amount));
        return true;
    }

    private boolean set(CommandSender sender, String[] args) {
        if (!sender.hasPermission("xskill.set") && !sender.hasPermission("xskill.admin")) {
            sender.sendMessage(Text.color("&cNo permission."));
            return true;
        }
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Text.color("&cPlayers only."));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Text.color("&cUsage: /xskill set <id>"));
            return true;
        }
        String swordId = args[1];
        if (swordId.isBlank()) {
            sender.sendMessage(Text.color("&cInvalid id."));
            return true;
        }
        SwordDefinition def = plugin.swords().get(swordId);
        if (def == null || !def.enabled()) {
            sender.sendMessage(Text.color("&cUnknown or disabled definition."));
            return true;
        }
        var hand = p.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            sender.sendMessage(Text.color("&cHold any item in your main hand."));
            return true;
        }
        if (!SwordItems.applySwordIdOnly(hand, def)) {
            sender.sendMessage(Text.color("&cCould not set ID on item."));
            return true;
        }
        sender.sendMessage(Text.color("&7Definition ID set on held item: &f" + swordId));
        return true;
    }

    private boolean stats(CommandSender sender, String[] args) {
        Player target;
        if (args.length >= 2) {
            if (!sender.hasPermission("xskill.stats.others") && !sender.hasPermission("xskill.admin")) {
                sender.sendMessage(Text.color("&cNo permission."));
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(Text.color("&cPlayer not found."));
                return true;
            }
        } else {
            if (!(sender instanceof Player p)) {
                sender.sendMessage(Text.color("&cConsole: /xskill stats <player>"));
                return true;
            }
            target = p;
        }
        int level = plugin.levels().level(target);
        double xp = plugin.levels().xp(target);
        double into = plugin.levels().xpIntoLevel(target);
        double toNext = plugin.levels().xpToNext(target);
        sender.sendMessage(Text.color("&6xSkill &7- &f" + target.getName()));
        sender.sendMessage(Text.color("&7Level: &a" + level));
        sender.sendMessage(Text.color("&7Total XP: &b" + String.format(Locale.ROOT, "%.2f", xp)));
        if (toNext > 0) {
            sender.sendMessage(Text.color("&7XP this level: &b" + String.format(Locale.ROOT, "%.2f", into) + "&7/&b" + String.format(Locale.ROOT, "%.2f", toNext)));
        } else {
            sender.sendMessage(Text.color("&7Max level."));
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
            return filter(Arrays.asList("give", "set", "reload", "stats", "level"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            var swords = plugin.swords().all().entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue().enabled())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            var players = Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            List<String> merged = new ArrayList<>(swords);
            merged.addAll(players);
            return filter(merged, args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return filter(plugin.swords().all().entrySet().stream()
                    .filter(e -> e.getValue() != null && e.getValue().enabled())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList()), args[1]);
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

