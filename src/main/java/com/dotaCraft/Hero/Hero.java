package com.dotaCraft.Hero;

import com.dotaCraft.Ability.Ability;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class Hero {
    public enum Attribute { STRENGTH, AGILITY, INTELLECT, UNIVERSAL }

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

    // Указываем явные дженерики
    private final Map abilitiesBySlot = new HashMap<>();

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

    public boolean castAbility(int slot) {
        Ability ability = (Ability) abilitiesBySlot.get(slot);
        if (ability == null) {
            player.sendMessage("§cAbility does not exist.");
            return false;
        }

        double manaCost = ability.getManaCost(ability.getAbilityLevel());
        /*if (currentMana < manaCost) {
            player.sendMessage("§bNot enough mana!");
            return false;
        }*/

        useMana(manaCost);
        ability.cast(this);
        return true;
    }

    public Hero getLastAttacker() {
        return null;
    }

    public void addAbility(int slot, Ability ability) {
        this.abilitiesBySlot.put(slot, ability);
    }

    public void addStrength(int amount) {
        baseStrength += amount;
    }

    // Getters & Stats calculations
    public Player getPlayer() { return player; }
    public String getHeroName() { return heroName; }
    public double getStrength() { return baseStrength + (strengthGain * (level - 1)); }
    public double getAgility() { return baseAgility + (agilityGain * (level - 1)); }
    public double getIntellect() { return baseIntellect + (intellectGain * (level - 1)); }
    public double getMaxHealth() { return 120.0 + (getStrength() * 22.0); }
    public double getMaxMana() { return 75.0 + (getIntellect() * 12.0); }
    public Ability getAbilityInSlot(int slot) { return (Ability) abilitiesBySlot.get(slot); }
    public int getLevel() { return level; }
}