package com.dotaCraft.Hero;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Item.Item;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class Hero {
    public enum Attribute { STRENGTH, AGILITY, INTELLECT, UNIVERSAL }

    private final Player player;
    private final String heroName;
    private final Attribute primaryAttribute;

    private int baseStrength;
    private int baseAgility;
    private int baseIntellect;

    private double strengthGain;
    private double agilityGain;
    private double intellectGain;

    private static int currentHealth;
    private static int currentMana;

    private boolean isHeroDead = false;
    private int level = 1;

    private final Map abilitiesBySlot = new HashMap<>();
    private final Map itemsBySlot = new HashMap<>();

    public Hero(Player player, String heroName, Attribute primaryAttribute,
                int baseStrength, int baseAgility, int baseIntellect,
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

    public void addItem(int slot, Item item) {
        if (item != null) {
            item.onEquip(this);
        }
    }

    public void addStrength(int amount) {
        baseStrength += amount;
    }
    public void addAgility(int amount) { baseAgility += amount; }
    public void addIntellect(int amount) { baseIntellect += amount; }

    // Getters & Stats calculations
    public Player getPlayer() { return player; }
    public String getHeroName() { return heroName; }
    public int getStrength() { return (int) (baseStrength + (strengthGain * (level - 1))); }
    public int getAgility() { return (int) (baseAgility + (agilityGain * (level - 1))); }
    public int getIntellect() { return (int) (baseIntellect + (intellectGain * (level - 1))); }
    public int getMaxHealth() { return 120 + (getStrength() * 22); }
    public static int getCurrentHealth() { return currentHealth; }
    public int getMaxMana() { return 75 + (getIntellect() * 12); }
    public static int getCurrentMana() { return currentMana; }
    public Ability getAbilityInSlot(int slot) { return (Ability) abilitiesBySlot.get(slot); }
    public int getLevel() { return level; }
}