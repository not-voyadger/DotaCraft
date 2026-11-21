package com.dotaCraft.Ability;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Abstract base class representing a Dota ability.
 */
public abstract class Ability {
    public enum AbilityTypes { PASSIVE, UNIT_TARGET, POINT_TARGET, NO_TARGET, TOGGLE, AUTO_CAST, AURA }
    public enum DamageTypes { PHYSICAL, MAGICAL, PURE, NONE }
    public enum TargetTypes { ALLY, ENEMY, ALL, NONE }
    public enum DispelTypes { NONE, BASIC, STRONG }

    private final DamageTypes damageType;
    private final AbilityTypes abilityType;
    private final TargetTypes targetType;
    private final DispelTypes dispelType;

    private double[] damage;
    private double[] manaCost;
    private double[] healthCost;
    private double[] coolDown;
    private double[] castRange;
    private double[] effectRadius;
    private double[] duration;

    private double castPoint; // time to cast in ticks/ms

    private int abilityLevel;
    private int abilityMaxLevel;
    private int requiredHeroLevel;
    private long cooldownEndMillis = 0L;

    private boolean isInnate;
    private boolean hasScepterUpgrade;
    private boolean hasShardUpgrade;

    private String iconSymbol = "";

    private static final Map<UUID, BukkitTask> ACTIVE_CASTS = new HashMap<>();

    private final Map specialValues = new HashMap<>();

    public Ability(DamageTypes damageType, AbilityTypes abilityType, TargetTypes targetType, DispelTypes dispelType,
                   double[] damage, double[] manaCost, double[] healthCost, double[] coolDown, double[] castRange,
                   double castPoint, double[] effectRadius, double[] duration, int abilityLevel, int abilityMaxLevel,
                   int requiredHeroLevel, boolean isInnate, boolean hasScepterUpgrade, boolean hasShardUpgrade, String iconSymbol) {

        this.damageType = damageType;
        this.abilityType = abilityType;
        this.targetType = targetType;
        this.dispelType = dispelType;
        this.damage = damage;
        this.manaCost = manaCost;
        this.healthCost = healthCost;
        this.coolDown = coolDown;
        this.castRange = castRange;
        this.castPoint = castPoint;
        this.effectRadius = effectRadius;
        this.duration = duration;
        this.abilityLevel = abilityLevel;
        this.abilityMaxLevel = abilityMaxLevel;
        this.requiredHeroLevel = requiredHeroLevel;
        this.isInnate = isInnate;
        this.hasScepterUpgrade = hasScepterUpgrade;
        this.hasShardUpgrade = hasShardUpgrade;
        this.iconSymbol = iconSymbol;
    }

    /**
     * Executes the active cast behavior of the ability.
     *
     * @return true if the cast was successful
     */
    public abstract boolean cast(Hero hero);

    protected void startCastWithPoint(Hero hero, Runnable onComplete, Runnable onCancel) {
        Player player = hero.getPlayer();
        UUID uuid = player.getUniqueId();

        cancelCast(uuid);

        long delayTicks = Math.round(this.castPoint * 20.0);

        if (delayTicks <= 0) {
            onComplete.run();
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                ACTIVE_CASTS.remove(uuid);
                if (!player.isOnline() || player.isDead()) {
                    if (onCancel != null) onCancel.run();
                    return;
                }
                onComplete.run();
            }
        }.runTaskLater(DotaCraft.getInstance(), delayTicks);

        ACTIVE_CASTS.put(uuid, task);
    }

    public static void cancelCast(UUID playerUuid) {
        BukkitTask existing = ACTIVE_CASTS.remove(playerUuid);
        if (existing != null && !existing.isCancelled()) {
            existing.cancel();
        }
    }

    public void startCooldown() {
        startCooldown(1.0);
    }

    public void startCooldown(double cooldownReductionMultiplier) {
        double baseCdSeconds = getCoolDown(this.abilityLevel);
        long durationMillis = Math.round(baseCdSeconds * cooldownReductionMultiplier * 1000.0);
        this.cooldownEndMillis = System.currentTimeMillis() + durationMillis;
    }

    public boolean isOnCooldown() {
        return System.currentTimeMillis() < this.cooldownEndMillis;
    }

    public double getRemainingCooldown() {
        long remaining = this.cooldownEndMillis - System.currentTimeMillis();
        return remaining > 0 ? remaining / 1000.0 : 0.0;
    }

    // ------ Event Hooks / Callbacks ------

    public void onCast(Hero hero) {} // no usages - delete?

    public double onTakeDamage(Hero hero, double incomingDamage) {
        return incomingDamage;
    }

    public void onEnemyDeath(Hero hero, Hero victim, double radius) {}

    public void onTick(Hero hero) {}

    public void onUnitDeath(Hero owner, LivingEntity victim, Hero killer, double distanceBlocks) {}

    public void onLearn(Hero hero) {}

    // ------ Scaled Attribute Accessors ------

    public double getDamage(int level) {
        return getArrayValueByLevel(damage, level);
    }

    public double getManaCost(int level) {
        return getArrayValueByLevel(manaCost, level);
    }

    public double getHealthCost(int level) {
        return getArrayValueByLevel(healthCost, level);
    }

    public double getCoolDown(int level) {
        return getArrayValueByLevel(coolDown, level);
    }

    public double getCastRange(int level) {
        return DotaUnits.toBlocks(getArrayValueByLevel(castRange, level));
    }

    public double getEffectRadius(int level) {
        return DotaUnits.toBlocks(getArrayValueByLevel(effectRadius, level));
    }

    public double getDuration(int level) {
        return getArrayValueByLevel(duration, level);
    }

    // ------ Getters & Setters ------

    public int getAbilityLevel() {
        return abilityLevel;
    }

    public void setAbilityLevel(int abilityLevel) {
        this.abilityLevel = Math.min(abilityLevel, abilityMaxLevel);
    }

    public void resetCooldown() {
        this.cooldownEndMillis = 0L;
    }


    public DamageTypes getDamageType() { return damageType; }
    public AbilityTypes getAbilityType() { return abilityType; }
    public TargetTypes getTargetType() { return targetType; }
    public DispelTypes getDispelType() { return dispelType; }
    public double getCastPoint() { return castPoint; }
    public int getAbilityMaxLevel() { return abilityMaxLevel; }
    public int getRequiredHeroLevel() { return requiredHeroLevel; }
    public boolean isInnate() { return isInnate; }
    public boolean hasScepterUpgrade() { return hasScepterUpgrade; }
    public boolean hasShardUpgrade() { return hasShardUpgrade; }
    public String getIconSymbol() { return iconSymbol; }

    // ------ Helpers ------

    // Helper method to safely extract array values based on ability level index.
    private double getArrayValueByLevel(double[] array, int level) {
        if (level <= 0 || array == null || array.length == 0) return 0;
        int index = Math.min(level - 1, array.length - 1);
        return array[index];
    }
}