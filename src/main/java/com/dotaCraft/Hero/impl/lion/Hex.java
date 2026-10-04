package com.dotaCraft.Hero.impl.lion;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.HologramManager;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Hex extends Ability {
    public Hex() {
        super(
                DamageTypes.NONE,
                AbilityTypes.UNIT_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.STRONG,
                new double []{0,0,0,0},
                new double[]{110, 140, 170, 200},
                new double []{0,0,0,0},
                new double []{24,20,16,12},
                new double[]{500, 525, 550, 575},
                0,
                new double []{0,0,0,0},
                new double []{2,2.4,2.8,3.2},
                1,
                4,
                1,
                false,
                false,
                false);
    }

    @Override
    public void cast(Hero hero) {

        Player player = hero.getPlayer();

        double range = this.getCastRange(hero.getLevel());

        org.bukkit.util.RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.5,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        if (result == null || !(result.getHitEntity() instanceof LivingEntity targetEntity)) {
            return;
        }

        double durationSeconds = this.getDuration(hero.getLevel());
        int durationTicks = (int) (durationSeconds * 20);

        player.getWorld().playSound(player.getLocation(), "dotacraft:lion.lion_hex_cast", 0.8f, 1.0f);

        targetEntity.getWorld().spawnParticle(Particle.EXPLOSION, targetEntity.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3);

        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0, false, false));
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 3, false, false));
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 255, false, false));

        HologramManager.spawnHexIndicator(targetEntity, durationSeconds);

        Frog frog = targetEntity.getWorld().spawn(targetEntity.getLocation(), Frog.class, f -> {
            f.setBaby();
            f.setInvulnerable(true);
            f.setAI(false);
            f.setGravity(false);
            f.setSilent(true);
        });

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

                    frog.teleport(targetEntity.getLocation());

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

                //player.getWorld().playSound(player.getLocation(), "dotacraft:scythe_of_vyse.scythe_of_vyse_return", 0.8f, 1.0f);

                if (frog != null && frog.isValid()) {
                    frog.remove();
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