package com.dotaCraft.Ability;

import com.dotaCraft.Hero.Hero;

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

    private int castPoint; // time to cast
    private int abilityLevel;
    private int abilityMaxLevel;
    private int requiredHeroLevel;

    private boolean isInnate;
    private boolean hasScepterUpgrade;
    private boolean hasShardUpgrade;

    private final Map specialValues = new HashMap<>();

    public Ability(DamageTypes damageType, AbilityTypes abilityType, TargetTypes targetType, DispelTypes dispelType,
                   double[] damage, double[] manaCost, double[] healthCost, double[] coolDown, double[] castRange,
                   int castPoint, double[] effectRadius, double[] duration, int abilityLevel, int abilityMaxLevel,
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

    // Getters

    public double getDamage(int level) {
        if (level <= 0) return 0;
        int index = Math.min(level - 1, damage.length - 1);
        return damage[index];
    }
}