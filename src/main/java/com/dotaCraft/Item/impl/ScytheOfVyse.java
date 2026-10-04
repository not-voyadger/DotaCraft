package com.dotaCraft.Item.impl;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HologramManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class ScytheOfVyse extends Item {

    private static final double HEX_DURATION_SECONDS = 3.5;
    private static final int MANA_COST = 250;

    public ScytheOfVyse() {
        super("scythe_of_vyse", TargetTypes.UNIT_TARGET, 700, 20.0, MANA_COST, 800.0, 0, false);

        addStatBonus(StatType.INTELLECT, 30.0);
        addStatBonus(StatType.MANA_REGEN, 8.5);

        setRecipeComponents(List.of("mystic_stuff", "tiara_of_selemene", "recipe_scythe_of_vyse"));
    }

    @Override
    public void onUseUnitTarget(Player player, Entity target) {
        if (!(target instanceof LivingEntity targetEntity) || targetEntity.isDead()) {
            return;
        }

        int durationTicks = (int) (HEX_DURATION_SECONDS * 20);

        player.getWorld().playSound(player.getLocation(), "dotacraft:scythe_of_vyse.scythe_of_vyse_cast", 0.8f, 1.0f);
        /*player.getWorld().playSound(targetEntity.getLocation(), Sound.ENTITY_PIG_BIG_HURT, 1.0f, 1.0f);
        player.getWorld().playSound(targetEntity.getLocation(), Sound.ENTITY_PIG_MINI_HURT, 1.0f, 1.0f);
        player.getWorld().playSound(targetEntity.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1.0f, 1.0f);*/

        targetEntity.getWorld().spawnParticle(Particle.EXPLOSION, targetEntity.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3);

        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0, false, false));
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 3, false, false));
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 255, false, false));

        Pig pig = targetEntity.getWorld().spawn(targetEntity.getLocation(), Pig.class, p -> {
            p.setBaby();
            p.setInvulnerable(true);
            p.setAI(false);
            p.setGravity(false);
            p.setSilent(true);
        });

        HologramManager.spawnHexIndicator(targetEntity, HEX_DURATION_SECONDS);

        new BukkitRunnable() {
            private int ticksLived = 0;

            @Override
            public void run() {
                try {
                    if (!targetEntity.isValid() || targetEntity.isDead() || ticksLived >= durationTicks) {
                        cleanup();
                        cancel();
                        return;
                    }

                    pig.teleport(targetEntity.getLocation());

                    if (ticksLived % 5 == 0) {
                        targetEntity.getWorld().spawnParticle(
                                Particle.WITCH,
                                targetEntity.getLocation().add(0, 0.5, 0),
                                3, 0.2, 0.2, 0.2
                        );
                    }
                } finally {
                    ticksLived++;
                }
            }

            private void cleanup() {

                player.getWorld().playSound(player.getLocation(), "dotacraft:scythe_of_vyse.scythe_of_vyse_return", 0.8f, 1.0f);

                if (pig != null && pig.isValid()) {
                    pig.remove();
                }

                if (targetEntity.isValid()) {
                    targetEntity.getWorld().spawnParticle(Particle.POOF, targetEntity.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2);
                    targetEntity.removePotionEffect(PotionEffectType.INVISIBILITY);
                    targetEntity.removePotionEffect(PotionEffectType.SLOWNESS);
                    targetEntity.removePotionEffect(PotionEffectType.WEAKNESS);
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
    }
}