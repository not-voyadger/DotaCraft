package com.dotaCraft.Ability;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Utils.DotaUnits;

import java.util.HashMap;
import java.util.Map;

public abstract class Ability {
    public enum AbilityTypes { PASSIVE, UNIT_TARGET, POINT_TARGET, NO_TARGET, TOGGLE, AUTO_CAST }
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

    private boolean isInnate;
    private boolean hasScepterUpgrade;
    private boolean hasShardUpgrade;

    private final Map specialValues = new HashMap<>();

    public Ability(DamageTypes damageType, AbilityTypes abilityType, TargetTypes targetType, DispelTypes dispelType,
                   double[] damage, double[] manaCost, double[] healthCost, double[] coolDown, double[] castRange,
                   double castPoint, double[] effectRadius, double[] duration, int abilityLevel, int abilityMaxLevel,
                   int requiredHeroLevel, boolean isInnate, boolean hasScepterUpgrade, boolean hasShardUpgrade) {

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
    }

    public void onCast(Hero hero) {}

    public double onTakeDamage(Hero hero, double incomingDamage) {
        return incomingDamage;
    }

    public void onEnemyDeath(Hero hero, Hero victim, double radius) {}

    public void onTick(Hero hero) {}

    public abstract void cast(Hero hero);

    // --- Getters ---

    public int getAbilityLevel() {
        return abilityLevel;
    }

    public void setAbilityLevel(int abilityLevel) {
        this.abilityLevel = Math.min(abilityLevel, abilityMaxLevel);
    }

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

    // Вспомогательный метод, чтобы не дублировать проверки массивов
    private double getArrayValueByLevel(double[] array, int level) {
        if (level <= 0 || array == null || array.length == 0) return 0;
        int index = Math.min(level - 1, array.length - 1);
        return array[index];
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
}