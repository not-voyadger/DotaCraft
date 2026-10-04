package com.dotaCraft.Hero.impl.lion;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ManaDrain extends Ability {
    private static final double[] MANA_DRAIN_PER_SECOND = {20.0, 40.0, 60.0, 120.0};

    public ManaDrain() {
        super(
                DamageTypes.NONE,
                AbilityTypes.UNIT_TARGET,
                TargetTypes.ALL,
                DispelTypes.NONE,
                new double []{0,0,0,0},
                new double []{0,0,0,0},
                new double []{0,0,0,0},
                new double []{15,12,9,6},
                new double []{850,850,850,850},
                0.5,
                new double []{0,0,0,0},
                new double []{5,5,5,5},
                1,
                4,
                1,
                false,
                false,
                true);
    }

    @Override
    public void cast(Hero hero) {
        Player player = hero.getPlayer();
        if (player == null || !player.isOnline()) return;

        int level = getAbilityLevel();
        double castRangeBlocks = getCastRange(level);
        double maxBreakDistanceBlocks = castRangeBlocks + DotaUnits.toBlocks(400);
        double durationSeconds = getDuration(level);
        int totalTicks = (int) (durationSeconds * 20);

        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                castRangeBlocks,
                0.8,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        if (result == null || !(result.getHitEntity() instanceof LivingEntity target)) {
            player.sendMessage("§cNo target for Mana Drain!");
            return;
        }

        double manaPerSecond = MANA_DRAIN_PER_SECOND[Math.min(level - 1, MANA_DRAIN_PER_SECOND.length - 1)];
        double manaPerTick = manaPerSecond / 20.0;

        hero.setChanneling(true);

        player.getWorld().playSound(player.getLocation(), "dotacraft:lion.lion_mana_drain_cast", 0.8f, 1.0f);

        Location startCastLoc = player.getLocation().clone();

        new BukkitRunnable() {
            private int currentTick = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || target.isDead() || !target.isValid() || currentTick >= totalTicks) {
                    player.stopSound("dotacraft:lion.lion_mana_drain_cast");
                    stopChanneling();
                    cancel();
                    return;
                }

                if (hasMoved(startCastLoc, player.getLocation())) {
                    player.stopSound("dotacraft:lion.lion_mana_drain_cast");
                    stopChanneling();
                    player.sendMessage("ManaDrain stopped.");
                    cancel();
                    return;
                }

                double currentDistance = player.getLocation().distance(target.getLocation());
                if (currentDistance > maxBreakDistanceBlocks) {
                    player.stopSound("dotacraft:lion.lion_mana_drain_cast");
                    stopChanneling();
                    player.sendMessage("ManaDrain stopped.");
                    cancel();
                    return;
                }

                hero.addMana(manaPerTick);

                if (currentTick % 2 == 0) {
                    drawDrainBeam(player.getEyeLocation().subtract(0, 0.3, 0), target.getEyeLocation().subtract(0, 0.2, 0));
                }

                currentTick++;
            }

            private void stopChanneling() {
                hero.setChanneling(false);
            }

            private boolean hasMoved(Location from, Location to) {
                return from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
            }

        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
    }

    private void drawDrainBeam(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        double stepBlue = 0.35;
        Location current = start.clone();

        Particle.DustOptions blueDust = new Particle.DustOptions(Color.fromRGB(0, 180, 255), 0.6f);
        Particle.DustOptions purpleDust = new Particle.DustOptions(Color.fromRGB(128, 0, 255), 0.6f);

        for (double d = 0; d < distance; d += stepBlue) {
            current.add(direction.clone().multiply(stepBlue));
            start.getWorld().spawnParticle(Particle.DUST, current, 1, 0.0, 0.0, 0.0, 0.0, blueDust);
            start.getWorld().spawnParticle(Particle.DUST, current, 1, 0.0, 0.0, 0.0, 0.0, purpleDust);
        }
    }
}
