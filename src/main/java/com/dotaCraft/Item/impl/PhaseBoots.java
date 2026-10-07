package com.dotaCraft.Item.impl;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class PhaseBoots extends Item {
    private final int activeDuration = 3;

    public PhaseBoots() {
        super(
                "phase_boots",
                TargetTypes.NO_TARGET,
                0,
                8.0,
                0,
                0,
                0,
                false
        );

        addStatBonus(StatType.MOVEMENT_SPEED, 50.0);
        addStatBonus(StatType.ARMOR, 4.0);

        setRecipeComponents(List.of("boots_of_speed", "chainmail", "blades_of_attack"));
    }

    @Override
    public void onEquip(Hero hero) {
        if (hero.isRanged()) {
            addStatBonus(StatType.DAMAGE, 12.0);
        } else {
            addStatBonus(StatType.DAMAGE, 18.0);
        }
    }

    @Override
    public void onUseNoTarget(Player player) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        double speedMultiplier = hero.isRanged() ? 0.10 : 0.20;
        double speedBonus = hero.getMoveSpeed() * speedMultiplier;

        addStatBonus(StatType.MOVEMENT_SPEED, getStatBonus(StatType.MOVEMENT_SPEED) + speedBonus);
        hero.updateSpeedAttribute();

        player.getWorld().playSound(player.getLocation(), "dotacraft:items.phase_boots", 0.2f, 1.0f);

        int activeDurationTicks = activeDuration * 20;

        BukkitTask trailTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                Location loc = player.getLocation().add(0, 0.2, 0);

                player.getWorld().spawnParticle(
                        Particle.CLOUD,
                        loc,
                        2, 0.2, 0.1, 0.2, 0.02
                );

                Particle.DustOptions purpleDust = new Particle.DustOptions(Color.fromRGB(128, 0, 255), 0.6f);
                player.getWorld().spawnParticle(Particle.DUST, loc, 7, 0.2, 0.2, 0.2, 0.1, purpleDust);

                player.getWorld().spawnParticle(
                        Particle.CRIT,
                        loc,
                        3, 0.3, 0.3, 0.3, 0.1
                );
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 2L);

        Bukkit.getScheduler().runTaskLater(DotaCraft.getInstance(), () -> {
            trailTask.cancel();

            addStatBonus(StatType.MOVEMENT_SPEED, getStatBonus(StatType.MOVEMENT_SPEED) - speedBonus);
            if (player.isOnline()) {
                hero.updateSpeedAttribute();
            }
        }, activeDurationTicks);
    }
}
