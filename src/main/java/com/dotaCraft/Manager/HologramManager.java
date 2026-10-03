package com.dotaCraft.Manager;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramManager {

    public HologramManager() {}

    public static void spawnDamageIndicator(LivingEntity target, double damage, Ability.DamageTypes damageType) {
        ArmorStand hologram = target.getWorld().spawn(target.getLocation().add(0, 0.5, 0), ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setInvulnerable(true);
            armorStand.setCustomName("§f§l " + (int) damage);
            armorStand.setCustomNameVisible(true);
        });

        Bukkit.getScheduler().runTaskLater(com.dotaCraft.DotaCraft.getInstance(), hologram::remove, 15L);

        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.0f);
    }

    public static void spawnStunIndicator(LivingEntity target, double stunLength) {
        createEntityProgressBar(target, "STUNNED", stunLength, "§f");
    }

    public static void spawnHexIndicator(LivingEntity target, double hexLength) {
        createEntityProgressBar(target, "HEXED", hexLength, "§f");
    }

    public static void createEntityProgressBar(LivingEntity target, String title, double durationSeconds, String colorCode) {
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