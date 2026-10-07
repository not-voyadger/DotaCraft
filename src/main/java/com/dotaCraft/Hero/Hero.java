package com.dotaCraft.Hero;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HologramManager;
import com.dotaCraft.Manager.ItemManager;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

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

    private double currentGold = 600.0;

    private boolean isHeroDead = false;
    private int level = 1;

    private AttackType attackType;
    private double attackRange = 3.5;
    private double projectileSpeed = 20.0;

    private Hero lastAttacker;
    private long lastAttackTimestamp;
    private static final long DAMAGE_TIMEOUT_MS = 15_000;

    private int baseDamageMin;
    private int baseDamageMax;

    private boolean isChanneling = false;

    private int currentXp = 0;

    private double baseMoveSpeed = 300.0;

    private double baseHeroDayVision = 1800.0;
    private double baseHeroNightVision = 800.0;

    private final Map abilitiesBySlot = new HashMap<>();
    private final Map<Integer, Item> itemsBySlot = new HashMap<>();

    private static final int[] XP_VALUES = {
            240, 400, 520, 600, 680, 760, 800, 900, 1000, 1100,
            1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000, 2200,
            2400, 2600, 2800, 3000, 4000, 5000, 6000, 7000, 8000, 0
    };

    public Hero(Player player, String heroName, Attribute primaryAttribute,
                int baseStrength, int baseAgility, int baseIntellect, double baseHealthRegen, double baseManaRegen,
                double strengthGain, double agilityGain, double intellectGain, double baseAttackTime,
                AttackType attackType, double attackRange, double projectileSpeed, int baseDamageMin, int baseDamageMax, double baseMoveSpeed) {
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

        this.baseDamageMin = baseDamageMin;
        this.baseDamageMax = baseDamageMax;

        this.currentHealth = getMaxHealth();
        this.currentMana = getMaxMana();

        this.baseMoveSpeed = baseMoveSpeed;

        this.attackRange = DotaUnits.toBlocks(attackRange);
        this.projectileSpeed = DotaUnits.toBlocks(projectileSpeed);
    }

    public Hero(Player player, String heroName, Attribute primaryAttribute,
                int baseStrength, int baseAgility, int baseIntellect,
                double baseHealthRegen, double baseManaRegen,
                double strengthGain, double agilityGain, double intellectGain,
                double baseAttackTime, int baseDamageMin, int baseDamageMax, double baseMoveSpeed) {
        this(player, heroName, primaryAttribute,
                baseStrength, baseAgility, baseIntellect,
                baseHealthRegen, baseManaRegen,
                strengthGain, agilityGain, intellectGain,
                baseAttackTime,
                AttackType.MELEE, 3.5, 0.0, baseDamageMin, baseDamageMax, baseMoveSpeed);
    }

    public void onTick() {
        if (player != null && player.isOnline()) {
            double hpRegen = (baseHealthRegen + (getStrength() * 0.1)) / 20.0;
            double manaRegen = (baseManaRegen + (getIntellect() * 0.05)) / 20.0;
            double goldPerTick = 90.0 / 1200.0;

            addHealth(hpRegen);
            addMana(manaRegen);

            updateSpeedAttribute();
            addGold(goldPerTick);
            //player.sendMessage("Current gold: " + (int) this.currentGold);
        }
    }

    public int getRequiredXpToNextLevel(int currentLevel) {
        if (currentLevel <= 0) return XP_VALUES[0];

        if (currentLevel >= XP_VALUES.length) {
            return 0;
        }

        return XP_VALUES[currentLevel - 1];
    }

    public void addXp(int amount) {
        if (amount < 0) {
            return;
        }

        this.currentXp += amount;
        if (currentXp >= (getRequiredXpToNextLevel(this.level))) {
            currentXp -= getRequiredXpToNextLevel(this.level);
            levelUp();
        }
    }

    public void addGold(double amount) {
        if (amount < 0) {
            return;
        }

        this.currentGold += amount;
    }

    public void spendGold(double amount) {
        if (amount < 0) {
            return;
        }

        this.currentGold -= amount;
    }

    public void buyItem(Item item) {
        int cost = item.getCost();

        if (cost < 0) {
            return;
        }

        spendGold(cost);
    }

    public void levelUp() {
        this.level++;
        this.player.sendMessage("Current level: " + this.level);

        player.playSound(player.getLocation(),Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);

        //Add stats
        int currentMaxStrength = getStrength();
        int currentMaxAgility = getAgility();
        int currentMaxIntellect = getIntellect();

        currentMaxStrength += this.strengthGain;
        currentMaxAgility += this.agilityGain;
        currentMaxIntellect += this.intellectGain;
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

        double manaCost = ability.getManaCost(ability.getAbilityLevel());
        if (currentMana < manaCost) {
            player.sendMessage("§bNot enough mana! Needs: " + manaCost + ", Current: " + currentMana);
            player.getWorld().playSound(player.getLocation(), "dotacraft:ui.ui_deny_mana", 0.8f, 1.0f);
            return false;
        }

        boolean castSuccess = ability.cast(this);

        if (castSuccess) {
            useMana(manaCost);
            return true;
        }

        return false;
    }

    public void launchProjectile(Hero attacker, LivingEntity target, double damage) {
        // Default behavior for Melee heroes.
    }

    protected void onProjectileHit(LivingEntity target, double damage) {
        boolean isLowGround = target.getLocation().getY() - player.getLocation().getY() > 0.5;
        boolean isMiss = isLowGround && ThreadLocalRandom.current().nextInt(100) < 25;

        if (isMiss) {
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0f, 1.5f);
            HologramManager.spawnMissIndicator(target);
        } else {
            target.setNoDamageTicks(0);
            DamageManager.dealDamage(this, target, damage, Ability.DamageTypes.PHYSICAL);
            HologramManager.spawnDamageIndicator(target, damage, Ability.DamageTypes.PHYSICAL);
            target.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, target.getLocation(), 10, 0.2, 0.2, 0.2, 0.1);
            target.getWorld().playSound(target.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.2f);
        }
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
                player.getWorld().playSound(player.getLocation(), "dotacraft:ui.ui_deny_cooldown", 0.5f, 1.0f);
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

    public void removeItemFromSlot(int slot) {
        this.itemsBySlot.remove(slot);
    }

    public int getSlotOfItem(Item item) {
        if (item == null) return -1;

        for (Map.Entry entry : itemsBySlot.entrySet()) {
            if (entry.getValue().equals(item)) {
                return (int) entry.getKey();
            }
        }
        return -1;
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

    public boolean isChanneling() {
        return isChanneling;
    }

    public void setChanneling(boolean channeling) {
        this.isChanneling = channeling;
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

            ItemManager.tryCraftItems(this);
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

        int randomBase = ThreadLocalRandom.current().nextInt(baseDamageMin, baseDamageMax + 1);
        return randomBase + primaryAttrValue + bonusDamage;
    }

    public void setMainDamage(double bonusBaseDamage) {
        this.baseDamageMin += bonusBaseDamage;
        this.baseDamageMax += bonusBaseDamage;
    }

    public boolean hasCleave() {
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item && item.getId().equalsIgnoreCase("battle_fury")) {
                return true;
            }
        }
        return false;
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

    public int getCurrentXp() {
        return currentXp;
    }

    public int getCurrentGold() {
        return (int) currentGold;
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

    public double getBonusMoveSpeed() {
        double bonus = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                bonus += item.getStatBonus(Item.StatType.MOVEMENT_SPEED);
            }
        }
        return bonus;
    }

    public double getMoveSpeed() {
        return baseMoveSpeed + getBonusMoveSpeed();
    }

    public void updateSpeedAttribute() {
        if (player == null || !player.isOnline()) return;

        double mcSpeed = (getMoveSpeed() / 300.0) * 0.1;

        var attribute = player.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.setBaseValue(mcSpeed);
        }
    }

    public double getCritChance() {
        double highestChance = 0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                highestChance = Math.max(highestChance, item.getStatBonus(Item.StatType.CRIT_CHANCE));
            }
        }
        return highestChance;
    }

    public double getCritMultiplier() {
        double highestMultiplier = 1.0;
        for (Object obj : itemsBySlot.values()) {
            if (obj instanceof Item item) {
                if (item.getStatBonus(Item.StatType.CRIT_CHANCE) > 0) {
                    highestMultiplier = Math.max(highestMultiplier, item.getStatBonus(Item.StatType.CRIT_MULTIPLIER));
                }
            }
        }
        return highestMultiplier;
    }

    public double getDayVision() {
        return DotaUnits.toBlocks(baseHeroDayVision);
    }

    public double getNightVision() {
        return DotaUnits.toBlocks(baseHeroNightVision);
    }

    public double getCurrentVisionRadius() {
        if (player == null || player.getWorld() == null) return getDayVision();

        long time = player.getWorld().getTime();

        boolean isDay = time < 12300 || time > 23850;
        return isDay ? getDayVision() : getNightVision();
    }

    public boolean isInUnexploredArea() {
        if (player == null || player.getWorld() == null) return false;

        return true;
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