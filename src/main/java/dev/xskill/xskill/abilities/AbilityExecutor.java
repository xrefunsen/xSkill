package dev.xskill.xskill.abilities;

import dev.xskill.xskill.XSkillPlugin;
import dev.xskill.xskill.model.SwordAbilityConfig;
import dev.xskill.xskill.model.SwordDefinition;
import dev.xskill.xskill.util.Cfg;
import dev.xskill.xskill.util.Text;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Locale;

public final class AbilityExecutor {
    private AbilityExecutor() {
    }

    public static boolean execute(XSkillPlugin plugin, Player player, SwordDefinition sword, SwordAbilityConfig ability) {
        if (!ability.enabled()) return false;
        if (!sword.enabled()) return false;
        if (plugin.levels().level(player) < sword.minLevel()) {
            player.sendMessage(Text.color("&cSeviyen yetmiyor. Gerekli: " + sword.minLevel()));
            return false;
        }

        long now = System.currentTimeMillis();
        String cdKey = sword.id() + ":" + ability.id();
        long left = plugin.cooldowns().remainingMs(player.getUniqueId(), cdKey, now);
        if (left > 0) {
            player.sendMessage(Text.color("&cCooldown: " + Math.ceil(left / 1000.0) + "s"));
            return false;
        }

        ConfigurationSection s = ability.section();
        int cooldownSeconds = Cfg.i(s, "cooldownSeconds", 0);
        boolean ok;
        String type = Cfg.str(s, "type", "").trim().toUpperCase(Locale.ROOT);
        ok = switch (type) {
            case "AOE_FREEZE" -> webFreeze(plugin, player, s);
            case "AOE_POTION" -> aoePotion(player, s);
            case "SELF_POTION" -> selfPotion(player, s);
            case "SELF_HEAL" -> heal(player, s);
            case "BLINK" -> blink(player, s);
            case "AOE_KNOCKUP" -> slam(player, s);
            case "AOE_LIGHTNING" -> lightning(player, s);
            case "VFX_RING" -> vfxRing(player, s);
            case "VFX_WAVE" -> vfxWave(plugin, player, s);
            default -> switch (ability.id().toLowerCase()) {
                case "webfreeze" -> webFreeze(plugin, player, s);
                case "heal" -> heal(player, s);
                case "regen" -> regen(player, s);
                case "blink" -> blink(player, s);
                case "speed" -> speed(player, s);
                case "slam" -> slam(player, s);
                case "aura" -> aura(player, s);
                case "darkness" -> darkness(player, s);
                case "lightning" -> lightning(player, s);
                default -> false;
            };
        };

        if (!ok) return false;
        plugin.cooldowns().set(player.getUniqueId(), cdKey, cooldownSeconds * 1000L, now);
        double xp = plugin.getConfig().getDouble("leveling.sources.abilityUse", 5.0);
        plugin.levels().addXp(player, xp);
        return true;
    }

    private static boolean vfxRing(Player player, ConfigurationSection s) {
        double radius = Cfg.d(s, "radius", 4.5);
        int points = Math.max(8, Cfg.i(s, "points", 48));
        double yOffset = Cfg.d(s, "yOffset", 0.1);
        Particle particle = Cfg.particle(s, "particle", Particle.DUST);
        double extra = Cfg.d(s, "extra", 0.0);
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return false;
        Location base = center.clone().add(0, yOffset, 0);
        Object data = particleData(s, particle);
        for (int i = 0; i < points; i++) {
            double a = (Math.PI * 2.0) * (i / (double) points);
            double x = Math.cos(a) * radius;
            double z = Math.sin(a) * radius;
            Location p = base.clone().add(x, 0, z);
            spawn(world, particle, p, 1, 0, 0, 0, extra, data);
        }
        Sound sound = Cfg.sound(s, "sound", null);
        if (sound != null) world.playSound(center, sound, 1.0f, 1.0f);
        return true;
    }

    private static boolean vfxWave(XSkillPlugin plugin, Player player, ConfigurationSection s) {
        double maxRadius = Cfg.d(s, "maxRadius", 6.0);
        double step = Math.max(0.2, Cfg.d(s, "step", 0.6));
        int points = Math.max(8, Cfg.i(s, "points", 40));
        double yOffset = Cfg.d(s, "yOffset", 0.1);
        int intervalTicks = Math.max(1, Cfg.i(s, "intervalTicks", 1));
        Particle particle = Cfg.particle(s, "particle", Particle.DUST);
        double extra = Cfg.d(s, "extra", 0.0);
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return false;
        Location base = center.clone().add(0, yOffset, 0);
        Object data = particleData(s, particle);
        Sound sound = Cfg.sound(s, "sound", null);
        if (sound != null) world.playSound(center, sound, 1.0f, 1.0f);

        new BukkitRunnable() {
            double r = step;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (r > maxRadius) {
                    cancel();
                    return;
                }
                for (int i = 0; i < points; i++) {
                    double a = (Math.PI * 2.0) * (i / (double) points);
                    double x = Math.cos(a) * r;
                    double z = Math.sin(a) * r;
                    Location p = base.clone().add(x, 0, z);
                    spawn(world, particle, p, 1, 0, 0, 0, extra, data);
                }
                r += step;
            }
        }.runTaskTimer(plugin, 0L, intervalTicks);

        return true;
    }

    private static Object particleData(ConfigurationSection s, Particle particle) {
        if (particle == Particle.DUST) {
            String hex = Cfg.str(s, "color", "#FF0000");
            float size = (float) Math.max(0.1, Cfg.d(s, "size", 1.6));
            Color c = parseColor(hex);
            try {
                return new Particle.DustOptions(c, size);
            } catch (Exception ignored) {
                return null;
            }
        }
        if (particle == Particle.BLOCK || particle == Particle.BLOCK_MARKER) {
            Material m = Cfg.material(s, "particleData", Material.REDSTONE_BLOCK);
            try {
                return m.createBlockData();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static Color parseColor(String hex) {
        if (hex == null) return Color.fromRGB(255, 0, 0);
        String h = hex.trim();
        if (h.startsWith("#")) h = h.substring(1);
        try {
            int v = Integer.parseInt(h, 16);
            int r = (v >> 16) & 255;
            int g = (v >> 8) & 255;
            int b = v & 255;
            return Color.fromRGB(r, g, b);
        } catch (Exception ignored) {
            return Color.fromRGB(255, 0, 0);
        }
    }

    private static void spawn(World world, Particle particle, Location loc, int count, double ox, double oy, double oz, double extra, Object data) {
        try {
            if (data != null) world.spawnParticle(particle, loc, count, ox, oy, oz, extra, data);
            else world.spawnParticle(particle, loc, count, ox, oy, oz, extra);
        } catch (Exception ignored) {
            world.spawnParticle(particle, loc, count, ox, oy, oz, extra);
        }
    }

    private static boolean webFreeze(XSkillPlugin plugin, Player player, ConfigurationSection s) {
        double radius = Cfg.d(s, "radius", 6.0);
        int durationSeconds = Cfg.i(s, "durationSeconds", 3);
        boolean applySlow = Cfg.b(s, "applyPotionSlow", true);
        int slowAmp = Cfg.i(s, "slowAmplifier", 10);
        boolean affectPlayers = Cfg.b(s, "targets.players", true);
        boolean affectMobs = Cfg.b(s, "targets.mobs", true);
        Sound sound = Cfg.sound(s, "sound", null);
        Particle particle = Cfg.particle(s, "particle", null);
        Material particleMat = Cfg.material(s, "particleData", Material.COBWEB);

        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return false;
        long until = System.currentTimeMillis() + durationSeconds * 1000L;

        List<Entity> nearby = player.getNearbyEntities(radius, radius, radius);
        boolean hit = false;
        for (Entity e : nearby) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e instanceof ArmorStand) continue;
            if (e.getUniqueId().equals(player.getUniqueId())) continue;
            if (e instanceof Player && !affectPlayers) continue;
            if (!(e instanceof Player) && !affectMobs) continue;
            if (le instanceof Player targetPlayer) {
                plugin.frozen().freeze(targetPlayer.getUniqueId(), until);
            }
            if (applySlow) {
                le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationSeconds * 20, slowAmp, true, true, true));
            }
            hit = true;
        }

        if (sound != null) world.playSound(center, sound, 1.0f, 1.0f);
        if (particle != null) {
            try {
                BlockData data = particleMat.createBlockData();
                world.spawnParticle(particle, center, 50, radius / 3.0, 0.8, radius / 3.0, 0.02, data);
            } catch (Exception ignored) {
                world.spawnParticle(particle, center, 50, radius / 3.0, 0.8, radius / 3.0, 0.02);
            }
        }
        return hit;
    }

    private static boolean aoePotion(Player player, ConfigurationSection s) {
        double radius = Cfg.d(s, "radius", 6.0);
        int durationSeconds = Cfg.i(s, "durationSeconds", 4);
        int amplifier = Cfg.i(s, "amplifier", 0);
        boolean affectPlayers = Cfg.b(s, "targets.players", true);
        boolean affectMobs = Cfg.b(s, "targets.mobs", true);
        PotionEffectType type = Cfg.potion(s, "potion", null);
        Sound sound = Cfg.sound(s, "sound", null);
        Particle particle = Cfg.particle(s, "particle", null);
        if (type == null) return false;
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return false;
        boolean hit = false;
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e instanceof ArmorStand) continue;
            if (e.getUniqueId().equals(player.getUniqueId())) continue;
            if (e instanceof Player && !affectPlayers) continue;
            if (!(e instanceof Player) && !affectMobs) continue;
            le.addPotionEffect(new PotionEffect(type, durationSeconds * 20, amplifier, true, true, true));
            hit = true;
        }
        if (sound != null) world.playSound(center, sound, 1.0f, 1.0f);
        if (particle != null) world.spawnParticle(particle, center, 40, radius / 3.0, 0.8, radius / 3.0, 0.02);
        return hit;
    }

    private static boolean selfPotion(Player player, ConfigurationSection s) {
        int durationSeconds = Cfg.i(s, "durationSeconds", 6);
        int amplifier = Cfg.i(s, "amplifier", 0);
        PotionEffectType type = Cfg.potion(s, "potion", null);
        Sound sound = Cfg.sound(s, "sound", null);
        if (type == null) return false;
        player.addPotionEffect(new PotionEffect(type, durationSeconds * 20, amplifier, true, true, true));
        if (sound != null) player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        return true;
    }

    private static boolean heal(Player player, ConfigurationSection s) {
        double healHearts = Cfg.d(s, "healHearts", 6.0);
        Sound sound = Cfg.sound(s, "sound", null);
        double heal = Math.max(0.0, healHearts * 2.0);
        double max = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        player.setHealth(Math.min(max, player.getHealth() + heal));
        if (sound != null) player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        return true;
    }

    private static boolean regen(Player player, ConfigurationSection s) {
        int durationSeconds = Cfg.i(s, "durationSeconds", 6);
        int amp = Cfg.i(s, "amplifier", 1);
        Sound sound = Cfg.sound(s, "sound", null);
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, durationSeconds * 20, amp, true, true, true));
        if (sound != null) player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        return true;
    }

    private static boolean blink(Player player, ConfigurationSection s) {
        double maxDistance = Cfg.d(s, "maxDistance", 10.0);
        Sound sound = Cfg.sound(s, "sound", null);
        Location from = player.getLocation();
        World world = from.getWorld();
        if (world == null) return false;
        Location dirTarget = from.clone().add(from.getDirection().normalize().multiply(maxDistance));
        Location best = from.clone();
        for (double d = maxDistance; d >= 1.0; d -= 0.5) {
            Location probe = from.clone().add(from.getDirection().normalize().multiply(d));
            probe.setY(from.getY());
            if (world.getBlockAt(probe).isPassable() && world.getBlockAt(probe.clone().add(0, 1, 0)).isPassable()) {
                best = probe;
                break;
            }
        }
        if (best.distanceSquared(from) < 1.0) best = dirTarget;
        player.teleport(best);
        if (sound != null) player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        return true;
    }

    private static boolean speed(Player player, ConfigurationSection s) {
        int durationSeconds = Cfg.i(s, "durationSeconds", 8);
        int amp = Cfg.i(s, "amplifier", 1);
        Sound sound = Cfg.sound(s, "sound", null);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, durationSeconds * 20, amp, true, true, true));
        if (sound != null) player.getWorld().playSound(player.getLocation(), sound, 1.0f, 1.0f);
        return true;
    }

    private static boolean slam(Player player, ConfigurationSection s) {
        double radius = Cfg.d(s, "radius", 5.0);
        double knockUp = Cfg.d(s, "knockUp", 0.6);
        Sound sound = Cfg.sound(s, "sound", null);
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return false;
        boolean hit = false;
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e.getUniqueId().equals(player.getUniqueId())) continue;
            le.setVelocity(le.getVelocity().setY(knockUp));
            hit = true;
        }
        if (sound != null) world.playSound(center, sound, 1.0f, 1.0f);
        return hit;
    }

    private static boolean aura(Player player, ConfigurationSection s) {
        int durationSeconds = Cfg.i(s, "durationSeconds", 6);
        double radius = Cfg.d(s, "radius", 5.0);
        int amp = Cfg.i(s, "amplifier", 1);
        Sound sound = Cfg.sound(s, "sound", null);
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return false;
        boolean hit = false;
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e.getUniqueId().equals(player.getUniqueId())) continue;
            le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, durationSeconds * 20, amp, true, true, true));
            hit = true;
        }
        if (sound != null) world.playSound(center, sound, 1.0f, 1.0f);
        return hit;
    }

    private static boolean darkness(Player player, ConfigurationSection s) {
        int durationSeconds = Cfg.i(s, "durationSeconds", 4);
        double radius = Cfg.d(s, "radius", 6.0);
        Sound sound = Cfg.sound(s, "sound", null);
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return false;
        boolean hit = false;
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e.getUniqueId().equals(player.getUniqueId())) continue;
            le.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, durationSeconds * 20, 0, true, true, true));
            hit = true;
        }
        if (sound != null) world.playSound(center, sound, 1.0f, 1.0f);
        return hit;
    }

    private static boolean lightning(Player player, ConfigurationSection s) {
        double radius = Cfg.d(s, "radius", 8.0);
        int strikes = Cfg.i(s, "strikes", 2);
        Sound sound = Cfg.sound(s, "sound", null);
        Location center = player.getLocation();
        World world = center.getWorld();
        if (world == null) return false;
        if (sound != null) world.playSound(center, sound, 1.0f, 1.0f);
        boolean hit = false;
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e.getUniqueId().equals(player.getUniqueId())) continue;
            for (int i = 0; i < strikes; i++) {
                world.strikeLightningEffect(le.getLocation());
            }
            hit = true;
        }
        return hit;
    }
}

