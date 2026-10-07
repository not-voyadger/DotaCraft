package com.dotaCraft.Units;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.HologramManager;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class NeutralCreep {

    private final String id;
    private final String name;
    private final EntityType entityType;
    private final double maxHealth;
    private double currentHealth;
    private final double damage;
    private final int goldReward;
    private final int xpReward;

    private LivingEntity entity;

    public NeutralCreep(String id, String name, EntityType entityType, double maxHealth, double damage, int goldReward, int xpReward) {
        this.id = id;
        this.name = name;
        this.entityType = entityType;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.damage = damage;
        this.goldReward = goldReward;
        this.xpReward = xpReward;
    }

    public LivingEntity spawn(Location location) {
        if (location.getWorld() == null) return null;

        this.entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);

        if (this.entity instanceof Mob mob) {
            mob.setRemoveWhenFarAway(false);
        }

        AttributeInstance maxHealthAttribute = this.entity.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttribute != null) {
            maxHealthAttribute.setBaseValue(Math.max(20.0, maxHealth));
            this.entity.setHealth(maxHealthAttribute.getValue());
        }

        this.currentHealth = maxHealth;
        updateNameTag();

        return this.entity;
    }

    public void takeDamage(double amount, Hero attacker) {
        if (isDead()) return;

        this.currentHealth = Math.max(0, this.currentHealth - amount);

        if (entity != null && entity.isValid()) {
            entity.playEffect(EntityEffect.HIT);
            entity.setHealth(Math.max(0.1, this.currentHealth));
        }

        updateNameTag();

        if (this.currentHealth <= 0) {
            die(attacker);
        }
    }

    private void updateNameTag() {
        if (entity == null || !entity.isValid()) return;

        String healthBarColor = currentHealth > (maxHealth * 0.5) ? "§a" : (currentHealth > (maxHealth * 0.2) ? "§e" : "§c");

        entity.setCustomName(String.format("§e%s %s[%d/%d HP]", name, healthBarColor, (int) currentHealth, (int) maxHealth));
        entity.setCustomNameVisible(true);
    }

    public void die(Hero killer) {
        if (entity != null && entity.isValid()) {

            if (killer != null) {
                killer.addXp(xpReward);
                killer.addGold(goldReward);
                HologramManager.spawnGoldIndicator(entity, goldReward);
                killer.getPlayer().sendMessage("[Debug] Current xp:" + killer.getCurrentXp());
            }

            org.bukkit.Bukkit.getScheduler().runTask(com.dotaCraft.DotaCraft.getInstance(), () -> {
                if (entity.isValid()) {
                    entity.setHealth(0);
                }
            });
        }
    }

    public boolean isDead() {
        return currentHealth <= 0 || entity == null || !entity.isValid() || entity.isDead();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public EntityType getEntityType() { return entityType; }
    public double getMaxHealth() { return maxHealth; }
    public double getCurrentHealth() { return currentHealth; }
    public double getDamage() { return damage; }
    public int getGoldReward() { return goldReward; }
    public int getXpReward() { return xpReward; }
}