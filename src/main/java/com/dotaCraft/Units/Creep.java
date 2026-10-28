package com.dotaCraft.Units;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.sources.VisionSource;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public abstract class Creep implements VisionSource {

    private final String id;
    private final String name;
    private final EntityType entityType;
    private final String team; // "radiant", "dire", "neutral"

    private double maxHealth;
    private double currentHealth;
    private double damage;
    private double armor;

    private int goldReward;
    private int xpReward;

    private double visionRadius;
    private double attackRange;

    private LivingEntity entity;

    public Creep(String id, String name, EntityType entityType, String team,
                 double maxHealth, double damage, double armor,
                 int goldReward, int xpReward, double visionRadius, double attackRange) {
        this.id = id;
        this.name = name;
        this.entityType = entityType;
        this.team = team;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.damage = damage;
        this.armor = armor;
        this.goldReward = goldReward;
        this.xpReward = xpReward;
        this.visionRadius = visionRadius;
        this.attackRange = attackRange;
    }

    public LivingEntity spawn(Location location) {
        if (location.getWorld() == null) return null;

        this.entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);

        if (this.entity instanceof Mob mob) {
            mob.setRemoveWhenFarAway(false);
        }

        this.currentHealth = maxHealth;
        updateNameTag();
        return this.entity;
    }

    public abstract void onTick();

    public void takeDamage(double amount, Hero attacker) {
        if (isDead()) return;

        this.currentHealth = Math.max(0, this.currentHealth - amount);

        if (entity != null && entity.isValid()) {
            entity.playEffect(org.bukkit.EntityEffect.HIT);
        }

        updateNameTag();

        if (this.currentHealth <= 0) {
            die(attacker);
        }
    }

    public void die(Hero killer) {
        if (entity != null && entity.isValid()) {
            if (killer != null) {
                killer.addXp(xpReward);
                killer.addGold(goldReward);
                com.dotaCraft.Manager.HologramManager.spawnGoldIndicator(entity, goldReward);
            }

            com.dotaCraft.DotaCraft.getInstance().getHealthBarManager().removeBar(entity);

            org.bukkit.Bukkit.getScheduler().runTask(com.dotaCraft.DotaCraft.getInstance(), () -> {
                if (entity.isValid()) {
                    entity.setHealth(0);
                }
            });
        }
    }

    protected abstract void updateNameTag();

    public boolean isDead() {
        return currentHealth <= 0 || entity == null || !entity.isValid() || entity.isDead();
    }

    @Override
    public Location getLocation() { return entity != null ? entity.getLocation() : null; }

    @Override
    public double getVisionRadius() { return visionRadius; }

    @Override
    public String getTeam() { return team; }

    @Override
    public boolean isValid() { return !isDead(); }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public EntityType getEntityType() { return entityType; }
    public double getMaxHealth() { return maxHealth; }
    public double getCurrentHealth() { return currentHealth; }
    public void setCurrentHealth(double hp) { this.currentHealth = Math.min(maxHealth, hp); }
    public double getDamage() { return damage; }
    public double getArmor() { return armor; }
    public int getGoldReward() { return goldReward; }
    public int getXpReward() { return xpReward; }
    public double getAttackRange() { return attackRange; }
    public LivingEntity getEntity() { return entity; }
}