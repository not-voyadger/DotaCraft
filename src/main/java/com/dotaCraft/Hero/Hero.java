package com.dotaCraft.Hero;

import org.bukkit.entity.Player;

public class Hero {
    private final Player player;
    private final String heroName;
    private final Attribute primaryAttribute;

    private double baseStrength;
    private double baseAgility;
    private double baseIntellect;

    private double strengthGain;
    private double agilityGain;
    private double intellectGain;

    private double currentHealth;
    private double currentMana;

    private boolean isHeroDead = false;
    private int level = 1;

    public Hero(Player player, String heroName, Attribute primaryAttribute,
                double baseStrength, double baseAgility, double baseIntellect,
                double strengthGain, double agilityGain, double intellectGain) {

        this.player = player;
        this.heroName = heroName;
        this.primaryAttribute = primaryAttribute;

        this.baseStrength = baseStrength;
        this.baseAgility = baseAgility;
        this.baseIntellect = baseIntellect;

        this.strengthGain = strengthGain;
        this.agilityGain = agilityGain;
        this.intellectGain = intellectGain;

        this.currentHealth = getMaxHealth();
        this.currentMana = getMaxMana();
    }

    public boolean useMana(double amount) {
        if (currentMana >= amount) {
            currentMana -= amount;
            return true;
        }
        return false;
    }

    public void takeDamage(double damage) {
        if (isHeroDead) return;

        currentHealth -= damage;
        if (currentHealth <= 0) {
            currentHealth = 0;
            heroDie();
        }
    }

    public void levelUp() {
        level ++;
    }

    public void heroDie() {
        this.isHeroDead = true;
    }

    public double calculateBuyback(double netWorth) {
        return 200 + ( netWorth / 13.0 );
    }

    // Characteristics

    public double getStrength() {
        return baseStrength + (strengthGain * (level - 1));
    }

    public double getAgility() {
        return baseAgility + (agilityGain * (level - 1));
    }

    public double getIntellect() {
        return baseIntellect + (intellectGain * (level - 1));
    }

    public double getMaxHealth() {
        return 120.0 + (getStrength() * 22.0);
    }

    public double getMaxMana() {
        return 75.0 + (getIntellect() * 12.0);
    }

    //Getters

    public Player getPlayer() {
        return player;
    }

    public String getHeroName() {
        return heroName;
    }

    public double getCurrentHealth() {
        return currentHealth;
    }

    public double getCurrentMana() {
        return currentMana;
    }

    public boolean isDead() {
        return isHeroDead;
    }

    public Attribute getPrimaryAttribute() {
        return primaryAttribute;
    }
}