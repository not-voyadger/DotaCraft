package com.dotaCraft.Manager;

import com.dotaCraft.Hero.Hero;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

    public static void applyDisarm(LivingEntity targetEntity, double durationSeconds) {
        int ticks = secondsToTicks(durationSeconds);

        // TO DO
    }

    // Helpers
    public static int secondsToTicks(double seconds) {
        int ticks = (int) seconds * 20;
        return ticks;
    }
}
