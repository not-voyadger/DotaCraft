package com.dotaCraft.Manager;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class EffectManager {

    public static void applyStun(LivingEntity victim, double durationSeconds) {
        int ticks = secondsToTicks(durationSeconds);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 10, false, false, true));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, ticks, 10, false, false, false));
    }

    public static void applyRooted(LivingEntity targetEntity, double durationSeconds) {
        int ticks = secondsToTicks(durationSeconds);
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 255, false, false));
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, ticks, 255, false, false));
    }

    public static void applySilence(Hero targetHero, double durationSeconds) {
        int ticks = secondsToTicks(durationSeconds);

        // TO DO

        HologramManager.spawnSilencedIndicator((Entity) targetHero, ticks);
    }

    public static void applyHex(LivingEntity targetEntity, double durationSeconds, Class<? extends Entity> entityClass, String castSoundKey, String returnSoundKey) {
        int durationTicks = secondsToTicks(durationSeconds);

        Hero hero = (targetEntity instanceof Player player) ? HeroManager.getHero(player) : null;

        if (castSoundKey != null && !castSoundKey.isEmpty()) {
            targetEntity.getWorld().playSound(targetEntity.getLocation(), castSoundKey, 0.8f, 1.0f);
        }

        targetEntity.getWorld().spawnParticle(Particle.EXPLOSION, targetEntity.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3);

        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, durationTicks, 0, false, false));
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 3, false, false));
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 255, false, false));

        HologramManager.spawnHexIndicator(targetEntity, durationSeconds);

        Entity disguiseEntity = targetEntity.getWorld().spawn(targetEntity.getLocation(), entityClass, entity -> {
            if (entity instanceof Ageable ageable) {
                ageable.setBaby();
            }
            if (entity instanceof Frog frog) {
                frog.setVariant(Frog.Variant.COLD);
            }
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.setGravity(false);
        });

        if (disguiseEntity instanceof Mob mob) {
            mob.setAI(false);
        }

        new BukkitRunnable() {
            private int ticksLived = 0;

            @Override
            public void run() {
                try {
                    boolean isDead = (hero != null) ? hero.isHeroDead() : (!targetEntity.isValid() || targetEntity.isDead());

                    if (isDead || ticksLived >= durationTicks) {
                        cleanup();
                        cancel();
                        return;
                    }

                    disguiseEntity.teleport(targetEntity.getLocation());

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
                if (returnSoundKey != null && !returnSoundKey.isEmpty()) {
                    targetEntity.getWorld().playSound(targetEntity.getLocation(), returnSoundKey, 0.8f, 1.0f);
                }

                if (disguiseEntity != null && disguiseEntity.isValid()) {
                    disguiseEntity.remove();
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

    public static void applyDoom(LivingEntity targetEntity, double durationSeconds) {
        int ticks = secondsToTicks(durationSeconds);

        Hero hero = (targetEntity instanceof Player player) ? HeroManager.getHero(player) : null;

        HologramManager.spawnDoomedIndicator(targetEntity, ticks);

        if (hero != null) {
            hero.setRegenDisabled(true);
        }

        new BukkitRunnable() {
            int ticksLived = 0;

            @Override
            public void run() {
                boolean isDead = (hero != null) ? hero.isHeroDead() : (!targetEntity.isValid() || targetEntity.isDead());

                if (isDead || ticksLived >= ticks) {
                    cancel();
                    return;
                }

                // TODO: apply silence, mute, block ALL regen, block lifesteal, dispel all positive effects and passives.

                ticksLived++;
            }

            @Override
            public synchronized void cancel() throws IllegalStateException {
                super.cancel();
                if (hero != null) {
                    hero.setRegenDisabled(false);
                }
            }

        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
    }

    public static void applyDisarm(LivingEntity targetEntity, double durationSeconds) {
        int ticks = secondsToTicks(durationSeconds);

        // TO DO
    }

    // Helpers
    public static int secondsToTicks(double seconds) {
        return (int) (seconds * 20.0);
    }
}