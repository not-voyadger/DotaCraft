package com.dotaCraft.Item;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Item {
    public enum TargetTypes { NO_TARGET, UNIT_TARGET, POINT_TARGET, PASSIVE, TOGGLE }

    private final String id;
    private final TargetTypes targetType;
    private final int cost;

    private final double coolDown;
    private final double manaCost;
    private final double castRange;

    private final int maxCharges;
    private int currentCharges;

    private final boolean isSellable;
    private final boolean isRecipe;
    private final boolean isConsumable;
    private final boolean isDropable;
    private final boolean isShareable;

    private long lastUsedTime = 0;

    protected Map<StatType, Double> statBonuses = new HashMap<>();
    private List recipeComponents;

    public enum StatType {
        STRENGTH, AGILITY, INTELLECT, DAMAGE, ARMOR, HEALTH_REGEN, MANA_REGEN
    }

    public Item(String id, TargetTypes targetType, int cost,
                double coolDown, double manaCost, double castRange,
                int maxCharges, boolean isConsumable) {
        this.id = id;
        this.targetType = targetType;
        this.cost = cost;
        this.coolDown = coolDown;
        this.manaCost = manaCost;
        this.castRange = castRange;
        this.maxCharges = maxCharges;
        this.currentCharges = 0; // start with 0

        this.isConsumable = isConsumable;
        this.isSellable = true;
        this.isRecipe = false;
        this.isDropable = true;
        this.isShareable = false;
    }

    // Helper to add statbonuses
    protected void addStatBonus(StatType stat, double val) {
        statBonuses.put(stat, val);
    }

    public double getStatBonus(StatType stat) {
        return (double) statBonuses.getOrDefault(stat, 0.0);
    }

    public void onEquip(Hero hero) {}

    public boolean canCast(Player player) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return false;

        boolean hasMana = hero.getCurrentMana() >= manaCost;
        boolean notOnCooldown = !isOnCooldown();

        return hasMana && notOnCooldown;
    }

    public boolean isOnCooldown() {
        if (coolDown <= 0) return false;
        return (System.currentTimeMillis() - lastUsedTime) < (long) (coolDown * 1000);
    }

    public void startCooldown() {
        this.lastUsedTime = System.currentTimeMillis();
    }

    public void onUseNoTarget(Player player) { }
    public void onUseUnitTarget(Player player, Entity target) { }
    public void onUsePointTarget(Player player, Location targetLocation) { }
    public void onToggle(Player player, boolean active) { }

    // Getters
    public String getId() { return id; }
    public TargetTypes getTargetType() { return targetType; }
    public int getCost() { return cost; }
    public double getCoolDown() { return coolDown; }
    public double getManaCost() { return manaCost; }
    public double getCastRange() { return castRange; }
    public int getCurrentCharges() { return currentCharges; }
    public int getMaxCharges() { return maxCharges; }
}