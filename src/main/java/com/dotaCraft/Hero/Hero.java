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
    public enum AttackType { MELEE, RANGED }
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

    private double currentHealth;
    private double currentMana;
    private double baseAttackTime = 1.7;

    private long lastAttackTime = 0;

    private boolean isHeroDead = false;
    private int level = 1;

    private AttackType attackType;
    private double attackRange = 3.5;
    private double projectileSpeed = 20.0;

    private Hero lastAttacker;
    private long lastAttackTimestamp;
    private static final long DAMAGE_TIMEOUT_MS = 15_000;

    private final Map abilitiesBySlot = new HashMap<>();
    private final Map itemsBySlot = new HashMap<>();

    public Hero(Player player, String heroName, Attribute primaryAttribute,
                int baseStrength, int baseAgility, int baseIntellect, double baseHealthRegen, double baseManaRegen,
                double strengthGain, double agilityGain, double intellectGain, double baseAttackTime, AttackType attackType, double attackRange, double projectileSpeed) {
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
        this.baseAttackTime = baseAttackTime;

        this.attackType = attackType;
        this.attackRange = attackRange;
        this.projectileSpeed = projectileSpeed;

        this.currentHealth = getMaxHealth();
        this.currentMana = getMaxMana();
    }

    public Hero(Player player, String heroName, Attribute primaryAttribute,
                int baseStrength, int baseAgility, int baseIntellect,
                double baseHealthRegen, double baseManaRegen,
                double strengthGain, double agilityGain, double intellectGain,
                double baseAttackTime) {
        this(player, heroName, primaryAttribute,
                baseStrength, baseAgility, baseIntellect,
                baseHealthRegen, baseManaRegen,
                strengthGain, agilityGain, intellectGain,
                baseAttackTime,
                AttackType.MELEE, 3.5, 0.0);
    }

    public void onTick() {
        if (player != null && player.isOnline()) {
            double hpRegen = (baseHealthRegen + (getStrength() * 0.1)) / 20.0;
            double manaRegen = (baseManaRegen + (getIntellect() * 0.05)) / 20.0;

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

        if (ability.getAbilityLevel() <= 0) {
            player.sendMessage("§cAbility is not leveled up!");
            return false;
        }

        if (ability == null) {
            player.sendMessage("§cAbility in slot " + slot + " does not exist.");
            return false;
        }

        double manaCost = ability.getManaCost(ability.getAbilityLevel());
        if (currentMana < manaCost) {
            player.sendMessage("§bNot enough mana! Needs: " + manaCost + ", Current: " + currentMana);
            player.getWorld().playSound(player.getLocation(), "dotacraft:ui.ui_deny_mana", 0.8f, 1.0f);
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
        if (lastAttacker == null) {
            return null;
        }

        // Если прошло больше 15 секунд с последнего удара, считать, что бой завершился
        if (System.currentTimeMillis() - lastAttackTimestamp > DAMAGE_TIMEOUT_MS) {
            this.lastAttacker = null;
            return null;
        }

        return lastAttacker;
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
        this.currentHealth = Math.min(this.currentHealth + amount, getMaxHealth());
    }

    public void addMana(double amount) {
        this.currentMana = Math.min(this.currentMana + amount, getMaxMana());
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

    public double getBaseAttackTime() {
        return baseAttackTime;
    }

    public double getAttackSpeed() {
        double speedFromItems = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                speedFromItems += item.getStatBonus(Item.StatType.ATTACK_SPEED);
            }
        }
        return 100.0 + getAgility() + speedFromItems;
    }

    public long getAttackIntervalMillis() {
        double attacksPerSecond = (getAttackSpeed() / 100.0) / getBaseAttackTime();
        return (long) (1000.0 / attacksPerSecond);
    }

    public boolean canAttack() {
        return (System.currentTimeMillis() - lastAttackTime) >= getAttackIntervalMillis();
    }

    public void resetAttackCooldown() {
        this.lastAttackTime = System.currentTimeMillis();
    }

    public int getBonusStrength() {
        int bonus = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                bonus += (int) item.getStatBonus(Item.StatType.STRENGTH);
            }
        }
        return bonus;
    }

    public double getMainDamage() {
        int primaryAttrValue = switch (primaryAttribute) {
            case Attribute.STRENGTH -> getStrength();
            case Attribute.AGILITY -> getAgility();
            case Attribute.INTELLECT -> getIntellect();
            case Attribute.UNIVERSAL -> (int) ((getStrength() + getAgility() + getIntellect()) * 0.7);
        };

        double bonusDamage = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                bonusDamage += item.getStatBonus(Item.StatType.DAMAGE);
            }
        }

        return primaryAttrValue + bonusDamage;
    }

    public boolean hasCleave() {
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item && item.getId().equalsIgnoreCase("battle_fury")) {
                return true;
            }
        }
        return false;
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

    public double getArmor() {
        double armorFromAgility = getAgility() * 0.166;
        double armorFromItems = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                armorFromItems += item.getStatBonus(Item.StatType.ARMOR);
            }
        }
        return armorFromAgility + armorFromItems;
    }

    public double getMagicResist() {
        double baseResist = 0.25;
        double intBonus = (getIntellect() * 0.1) / 100.0; // +0.1% for intellect
        return Math.min(0.9, baseResist + intBonus);
    }

    public void setLastAttacker(Hero attacker) {
        if (attacker == null || attacker.equals(this)) return;
        this.lastAttacker = attacker;
        this.lastAttackTimestamp = System.currentTimeMillis();
    }

    public int getStrength() { return getBaseStrength() + getBonusStrength(); }
    public int getAgility() { return getBaseAgility() + getBonusAgility(); }
    public int getIntellect() { return getBaseIntellect() + getBonusIntellect(); }

    public int getMaxHealth() { return 120 + (getStrength() * 22); }
    public int getCurrentHealth() { return (int) currentHealth; }
    public int getMaxMana() { return 75 + (getIntellect() * 12); }
    public int getCurrentMana() { return (int) currentMana; }
    public Ability getAbilityInSlot(int slot) { return (Ability) abilitiesBySlot.get(slot); }
    public Item getItemInSlot(int slot) { return (Item) itemsBySlot.get(slot); }
    public int getLevel() { return level; }

    public AttackType getAttackType() { return attackType; }
    public boolean isRanged() { return attackType == AttackType.RANGED; }

    public double getAttackRange() { return attackRange; }

    public double getProjectileSpeed() { return projectileSpeed; }

    // Setters

    public void setBaseAttackTime(double baseAttackTime) {
        this.baseAttackTime = baseAttackTime;
    }
    public void setAttackType(AttackType attackType) { this.attackType = attackType; }
    public void setAttackRange(double attackRange) { this.attackRange = attackRange; }
    public void setProjectileSpeed(double projectileSpeed) { this.projectileSpeed = projectileSpeed; }

}