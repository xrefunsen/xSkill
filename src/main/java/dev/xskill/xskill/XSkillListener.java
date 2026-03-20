package dev.xskill.xskill;

import dev.xskill.xskill.abilities.AbilityExecutor;
import dev.xskill.xskill.items.SwordItems;
import dev.xskill.xskill.model.AbilityTrigger;
import dev.xskill.xskill.model.SwordAbilityConfig;
import dev.xskill.xskill.model.SwordDefinition;
import dev.xskill.xskill.util.Text;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class XSkillListener implements Listener {
    private final XSkillPlugin plugin;
    private final Map<UUID, Long> lastFrozenBarMs = new ConcurrentHashMap<>();
    private final Map<UUID, Long> combatTagUntilMs = new ConcurrentHashMap<>();

    public XSkillListener(XSkillPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.SPECTATOR) return;
        if (isWorldDisabled(p)) return;
        if (isCombatBlocked(p)) return;
        Action a = e.getAction();
        boolean rightClick = a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK;
        if (!rightClick) return;

        ItemStack item = p.getInventory().getItemInMainHand();
        String swordId = SwordItems.getSwordId(item);
        if (swordId == null) return;
        SwordDefinition def = plugin.swords().get(swordId);
        if (def == null || !def.enabled()) return;

        AbilityTrigger trig = p.isSneaking() ? AbilityTrigger.SHIFT_RIGHT_CLICK : AbilityTrigger.RIGHT_CLICK;
        boolean usedAny = false;
        for (SwordAbilityConfig ab : def.abilities().values()) {
            if (ab.trigger() != trig) continue;
            usedAny |= AbilityExecutor.execute(plugin, p, def, ab);
        }
        if (usedAny) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        ItemStack item = p.getInventory().getItemInMainHand();
        String swordId = SwordItems.getSwordId(item);
        if (swordId == null) return;
        SwordDefinition def = plugin.swords().get(swordId);
        if (def == null || !def.enabled()) return;

        if (plugin.levels().level(p) < def.minLevel()) return;

        if (def.damageBonus() != 0.0) {
            e.setDamage(Math.max(0.0, e.getDamage() + def.damageBonus()));
        }

        tagCombat(p);
        if (e.getEntity() instanceof Player victim) tagCombat(victim);

        double xp = plugin.getConfig().getDouble("leveling.sources.hit", 1.0);
        plugin.levels().addXp(p, xp);

        if (def.combatSection() == null) return;
        if (def.combatSection().getBoolean("onHit.enabled", false)) {
            String potion = def.combatSection().getString("onHit.potion", "SLOWNESS");
            int dur = def.combatSection().getInt("onHit.durationSeconds", 2);
            int amp = def.combatSection().getInt("onHit.amplifier", 0);
            if (e.getEntity() instanceof org.bukkit.entity.LivingEntity le) {
                org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(potion.toUpperCase());
                if (type != null) {
                    le.addPotionEffect(new org.bukkit.potion.PotionEffect(type, dur * 20, amp, true, true, true));
                }
            }
        }
        if (def.combatSection().getBoolean("knockback.enabled", false)) {
            double strength = def.combatSection().getDouble("knockback.strength", 1.0);
            Entity target = e.getEntity();
            target.setVelocity(target.getVelocity().add(p.getLocation().getDirection().normalize().multiply(strength)));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKill(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer == null) return;
        tagCombat(killer);
        ItemStack item = killer.getInventory().getItemInMainHand();
        String swordId = SwordItems.getSwordId(item);
        if (swordId == null) return;
        SwordDefinition def = plugin.swords().get(swordId);
        if (def == null || !def.enabled()) return;
        double xp = plugin.getConfig().getDouble("leveling.sources.kill", 10.0);
        plugin.levels().addXp(killer, xp);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!plugin.frozen().isFrozen(p.getUniqueId(), System.currentTimeMillis())) return;
        if (e.getFrom().getWorld() == null || e.getTo() == null) return;
        if (e.getFrom().distanceSquared(e.getTo()) == 0.0) return;
        e.setTo(e.getFrom());
        long now = System.currentTimeMillis();
        long last = lastFrozenBarMs.getOrDefault(p.getUniqueId(), 0L);
        if (now - last >= 900L) {
            lastFrozenBarMs.put(p.getUniqueId(), now);
            p.sendActionBar(Text.component("&f&lFrozen!"));
        }
    }

    private boolean isWorldDisabled(Player p) {
        var sec = plugin.getConfig().getConfigurationSection("swords.settings");
        if (sec == null) return false;
        Set<String> disabled = new HashSet<>(sec.getStringList("disabledWorlds"));
        if (disabled.isEmpty()) return false;
        String w = p.getWorld().getName();
        for (String s : disabled) {
            if (s != null && s.equalsIgnoreCase(w)) return true;
        }
        return false;
    }

    private void tagCombat(Player p) {
        var sec = plugin.getConfig().getConfigurationSection("swords.settings.combatTag");
        if (sec == null) return;
        if (!sec.getBoolean("enabled", false)) return;
        int seconds = Math.max(1, sec.getInt("seconds", 10));
        combatTagUntilMs.put(p.getUniqueId(), System.currentTimeMillis() + seconds * 1000L);
    }

    private boolean isCombatBlocked(Player p) {
        var sec = plugin.getConfig().getConfigurationSection("swords.settings.combatTag");
        if (sec == null) return false;
        if (!sec.getBoolean("enabled", false)) return false;
        long until = combatTagUntilMs.getOrDefault(p.getUniqueId(), 0L);
        if (until <= System.currentTimeMillis()) return false;
        if (sec.getBoolean("blockAbilitiesWhileTagged", false)) {
            p.sendActionBar(Text.component("&cCannot use while in combat"));
            return true;
        }
        return false;
    }
}

