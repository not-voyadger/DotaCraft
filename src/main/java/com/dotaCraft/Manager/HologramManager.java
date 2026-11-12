package com.dotaCraft.Manager;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class HologramManager {

    public HologramManager() {}

    public static void spawnDamageIndicator(Entity target, double damage, Ability.DamageTypes damageType) {
        if (target == null || !target.isValid()) return;

        double offsetX = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;
        double offsetZ = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.8;

        ArmorStand hologram = target.getWorld().spawn(target.getLocation().add(offsetX, 0.5, offsetZ), ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomName("§f§l " + (int) damage);
            armorStand.setCustomNameVisible(true);
        });

        Bukkit.getScheduler().runTaskLater(com.dotaCraft.DotaCraft.getInstance(), hologram::remove, 15L);

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.7f, 1.0f);
    }

    // Health regen indicator is shown only to the players in the same team
    public static void spawnHealthRegenerationIndicator(Hero targetHero, double amount) {
        Player targetPlayer = targetHero.getPlayer();
        String team = targetHero.getTeam();

        double offsetX = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        double offsetZ = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        Location spawnLoc = targetPlayer.getLocation().add(offsetX, 1.8, offsetZ);

        String formattedAmount = (amount % 1 == 0)
                ? String.valueOf((int) amount)
                : String.format("%.1f", amount);

        ArmorStand hologram = spawnLoc.getWorld().spawn(spawnLoc, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setSmall(true);
            stand.setCustomName(ChatColor.GREEN + "+" + formattedAmount);
            stand.setCustomNameVisible(true);
        });

        Plugin plugin = DotaCraft.getInstance();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Hero otherHero = HeroManager.getHero(onlinePlayer);
            if (otherHero == null || !otherHero.getTeam().equals(team)) {
                onlinePlayer.hideEntity(plugin, hologram);
            }
        }

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!hologram.isValid() || hologram.getTicksLived() >= 20) {
                hologram.remove();
                task.cancel();
                return;
            }
            hologram.teleport(hologram.getLocation().add(0, 0.04, 0));
        }, 1L, 1L);
    }

    public static void spawnMissIndicator(Entity target) {
        Location targetLoc = target.getLocation().clone().add(0, target.getHeight() / 2.0, 0);

        target.getWorld().playSound(targetLoc, Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0f, 1.5f);

        // MISS
        ArmorStand hologram = target.getWorld().spawn(target.getLocation().clone().add(0, 1, 0), ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomName("§c§lMISS");
            armorStand.setCustomNameVisible(true);
        });

        Bukkit.getScheduler().runTaskLater(DotaCraft.getInstance(), hologram::remove, 12L);
    }

    public static void spawnCritIndicator(Entity target, double damage) {
        ArmorStand hologram = target.getWorld().spawn(target.getLocation().add(0, 0.8, 0), ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomName("§c§l " + (int) damage + "!");
            armorStand.setCustomNameVisible(true);
        });

        Bukkit.getScheduler().runTaskLater(DotaCraft.getInstance(), hologram::remove, 20L);

        target.getWorld().playSound(target.getLocation(), "dotacraft:ui.ui_critical_strike", 0.2f, 1.0f);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.0f);
    }

    public static void spawnGoldIndicator(Entity target, int amount) {
        ArmorStand hologram = target.getWorld().spawn(target.getLocation().add(1, 1.4, 0), ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomName("§e + " + amount);
            armorStand.setCustomNameVisible(true);
        });

        Bukkit.getScheduler().runTaskLater(DotaCraft.getInstance(), hologram::remove, 20L);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    public static void spawnStunIndicator(Entity target, double stunLength) {
        createEntityProgressBar(target, "STUNNED", stunLength, "§f");
    }

    public static void spawnHexIndicator(Entity target, double hexLength) {
        createEntityProgressBar(target, "HEXED", hexLength, "§f");
    }

    public static void spawnRootedIndicator(Entity target, double rootLength) {
        createEntityProgressBar(target, "ROOTED", rootLength, "§f");
    }

    public static void spawnSilencedIndicator(Entity target, double silenceLength) {
        createEntityProgressBar(target, "SILENCED", silenceLength, "§f");
    }

    public static void spawnDoomedIndicator(Entity target, double silenceLength) {
        createEntityProgressBar(target, "DOOMED", silenceLength, "§f");
    }

    public static void createEntityProgressBar(Entity target, String title, double durationSeconds, String colorCode) {
        if (target == null || target.isDead()) return;

        long totalTicks = (long) (durationSeconds * 20);
        int barLength = 10;

        double baseOffset = target.getHeight() + 0.3;
        double lineSpacing = 0.3;

        ArmorStand barHologram = target.getWorld().spawn(target.getLocation().add(0, baseOffset, 0), ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
        });

        ArmorStand titleHologram = target.getWorld().spawn(target.getLocation().add(0, baseOffset + lineSpacing, 0), ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomName(colorCode + "§l" + title);
            armorStand.setCustomNameVisible(true);
        });

        new BukkitRunnable() {
            long currentTick = totalTicks;

            @Override
            public void run() {
                if (target.isDead() || !target.isValid() || currentTick <= 0) {
                    barHologram.remove();
                    titleHologram.remove();
                    this.cancel();
                    return;
                }

                barHologram.teleport(target.getLocation().add(0, baseOffset, 0));
                titleHologram.teleport(target.getLocation().add(0, baseOffset + lineSpacing, 0));

                double progress = (double) currentTick / totalTicks;
                int filledBars = (int) Math.round(progress * barLength);

                StringBuilder barBuilder = new StringBuilder();
                barBuilder.append(colorCode);

                for (int i = 0; i < barLength; i++) {
                    if (i < filledBars) {
                        barBuilder.append("█");
                    } else {
                        barBuilder.append("§7▒");
                    }
                }

                barHologram.setCustomName(barBuilder.toString());
                currentTick--;
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
    }
}