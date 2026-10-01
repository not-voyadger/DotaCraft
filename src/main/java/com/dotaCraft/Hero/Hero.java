package com.dotaCraft.Hero;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Item.Item;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
    private double baseHealthRegen;
    private double baseManaRegen;

    private int currentHealth;
    private int currentMana;

    private boolean isHeroDead = false;
    private int level = 1;

    private final Map abilitiesBySlot = new HashMap<>();
    private final Map itemsBySlot = new HashMap<>();

    public Hero(Player player, String heroName, Attribute primaryAttribute,
                int baseStrength, int baseAgility, int baseIntellect, double baseHealthRegen, double baseManaRegen,
                double strengthGain, double agilityGain, double intellectGain) {
        this.player = player;
        this.heroName = heroName;
        this.primaryAttribute = primaryAttribute;
        this.baseStrength = baseStrength;
        this.baseAgility = baseAgility;
        this.baseIntellect = baseIntellect;
        this.baseHealthRegen = baseHealthRegen;
        this.baseManaRegen = baseManaRegen;
        this.strengthGain = strengthGain;
        this.agilityGain = agilityGain;
        this.intellectGain = intellectGain;

        this.currentHealth = getMaxHealth();
        this.currentMana = getMaxMana();
    }

    public void onTick() {
        if (player != null && player.isOnline()) {
            double hpRegen = baseHealthRegen + ( 0.05 + getStrength());
            double manaRegen = baseManaRegen + ( 0.05 + getIntellect());

            addHealth(hpRegen);
            addMana(manaRegen);
        }
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
            player.sendMessage("§cAbility in slot " + slot + " does not exist.");
            return false;
        }

        double manaCost = ability.getManaCost(ability.getAbilityLevel());
        if (currentMana < manaCost) {
            player.sendMessage("§bNot enough mana!");
            return false;
        }

        useMana(manaCost);
        ability.cast(this);
        return true;
    }

    public void useItem(int slot) {
        useItem(slot, null);
    }

    public void useItem(int slot, Entity manualTarget) {
        Item item = (Item) itemsBySlot.get(slot);
        if (item == null) {
            player.sendMessage("§cNo item in slot " + slot);
            return;
        }

        if (!item.canCast(player)) {
            if (item.isOnCooldown()) {
                player.sendMessage("§cItem is on cooldown!");
            } else if (currentMana < item.getManaCost()) {
                player.sendMessage("§bNot enough mana!");
            }
            return;
        }

        switch (item.getTargetType()) {
            case NO_TARGET -> {
                useMana(item.getManaCost());
                item.startCooldown();
                item.onUseNoTarget(this.player);
            }
            case UNIT_TARGET -> {
                Entity target = manualTarget;
                if (target == null) {
                    target = getTargetEntity(item.getCastRange());
                }

                if (target instanceof LivingEntity livingTarget) {
                    useMana(item.getManaCost());
                    item.startCooldown();
                    item.onUseUnitTarget(this.player, livingTarget);
                } else {
                    player.sendMessage("§cNo valid target for item!");
                }
            }
            case POINT_TARGET -> {
                Location targetLoc = player.getTargetBlockExact((int) item.getCastRange()) != null
                        ? player.getTargetBlockExact((int) item.getCastRange()).getLocation()
                        : player.getLocation();

                useMana(item.getManaCost());
                item.startCooldown();
                item.onUsePointTarget(this.player, targetLoc);
            }
            default -> {}
        }
    }

    private Entity getTargetEntity(double range) {
        var result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                entity -> !entity.equals(player) && entity instanceof LivingEntity
        );
        return result != null ? result.getHitEntity() : null;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.min(currentHealth, getMaxHealth());
    }

    public void setCurrentMana(int currentMana) {
        this.currentMana = Math.min(currentMana, getMaxMana());
    }

    public Hero getLastAttacker() {
        return null;
    }

    public void addAbility(int slot, Ability ability) {
        this.abilitiesBySlot.put(slot, ability);
    }

    public void addItem(int slot, Item item) {
        if (item != null) {
            this.itemsBySlot.put(slot, item);
            item.onEquip(this);
        }
    }

    public void addHealth(double amount) {
        this.currentHealth = (int) Math.min(this.currentHealth + amount, getMaxHealth());
    }

    public void addMana(double amount) {
        this.currentMana = (int) Math.min(this.currentMana + amount, getMaxMana());
    }

    public void addStrength(int amount) { baseStrength += amount; }
    public void addAgility(int amount) { baseAgility += amount; }
    public void addIntellect(int amount) { baseIntellect += amount; }

    // Getters
    public Player getPlayer() { return player; }
    public String getHeroName() { return heroName; }

    public int getBaseStrength() { return (int) (baseStrength + (strengthGain * (level - 1))); }
    public int getBaseAgility() { return (int) (baseAgility + (agilityGain * (level - 1))); }
    public int getBaseIntellect() { return (int) (baseIntellect + (intellectGain * (level - 1))); }

    public int getBonusStrength() {
        int bonus = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                bonus += (int) item.getStatBonus(Item.StatType.STRENGTH);
            }
        }
        return bonus;
    }

    public int getBonusAgility() {
        int bonus = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                bonus += (int) item.getStatBonus(Item.StatType.AGILITY);
            }
        }
        return bonus;
    }

    public int getBonusIntellect() {
        int bonus = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                bonus += (int) item.getStatBonus(Item.StatType.INTELLECT);
            }
        }
        return bonus;
    }

    public int getStrength() { return getBaseStrength() + getBonusStrength(); }
    public int getAgility() { return getBaseAgility() + getBonusAgility(); }
    public int getIntellect() { return getBaseIntellect() + getBonusIntellect(); }

    public int getMaxHealth() { return 120 + (getStrength() * 22); }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxMana() { return 75 + (getIntellect() * 12); }
    public int getCurrentMana() { return currentMana; }
    public Ability getAbilityInSlot(int slot) { return (Ability) abilitiesBySlot.get(slot); }
    public Item getItemInSlot(int slot) { return (Item) itemsBySlot.get(slot); }
    public int getLevel() { return level; }
}