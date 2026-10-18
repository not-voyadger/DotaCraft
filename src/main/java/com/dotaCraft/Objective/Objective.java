package com.dotaCraft.Objective;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.sources.VisionSource;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public abstract class Objective implements VisionSource {
    public enum TeamEnum { RADIANT, DIRE, NEUTRAL }

    private final String name;
    private final TeamEnum team;

    private int goldReward;
    private int xpReward;

    private double maxHealth;
    private double currentHealth;
    private double healthRegen;
    private double armor;

    private boolean isAlive = true;
    private boolean isDeniable = true;
    private boolean isGlyphActive = false;

    private Entity entity;
    private Location location;
    private double visionRadius;

    public Objective(String name, TeamEnum team, int goldReward, int xpReward,
                     double maxHealth, double healthRegen, double armor,
                     Location location, double visionRadius) {
        this.name = name;
        this.team = team;
        this.goldReward = goldReward;
        this.xpReward = xpReward;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.healthRegen = healthRegen;
        this.armor = armor;
        this.location = location;
        this.visionRadius = visionRadius;
    }

    public abstract void spawn(Location loc);

    public void onTick() {
        if (!isAlive) return;

        if (healthRegen > 0 && currentHealth < maxHealth) {
            addHealth(healthRegen / 20.0);
        }
    }

    public void takeDamage(Hero attacker, double rawDamage) {
        if (!isAlive || isGlyphActive) return;

        boolean isDenyAttempt = (attacker != null) &&
                attacker.getTeam().equalsIgnoreCase(getTeamName()) &&
                (currentHealth / maxHealth <= 0.10);

        if (attacker != null && attacker.getTeam().equalsIgnoreCase(getTeamName()) && !isDenyAttempt) {
            return;
        }

        double armorReduction = (0.06 * armor) / (1 + 0.06 * Math.abs(armor));
        double finalDamage = rawDamage * (1.0 - armorReduction);

        this.currentHealth = Math.max(0, this.currentHealth - finalDamage);
        updateNameTag();

        if (this.currentHealth <= 0) {
            onDeath(attacker, isDenyAttempt);
        }
    }

    protected abstract void updateNameTag();

    protected void onDeath(Hero killer, boolean isDenied) {
        this.isAlive = false;
        if (entity != null && entity.isValid()) {
            entity.remove();
        }
    }

    public void addHealth(double amount) {
        this.currentHealth = Math.min(maxHealth, currentHealth + amount);
        updateNameTag();
    }

    // --- VisionSource implementation ---
    @Override
    public Location getLocation() { return location; }

    @Override
    public double getVisionRadius() { return visionRadius; }

    @Override
    public String getTeam() { return getTeamName(); }

    @Override
    public boolean isValid() { return isAlive && entity != null && entity.isValid(); }

    public String getTeamName() {
        return team == TeamEnum.RADIANT ? "radiant" : (team == TeamEnum.DIRE ? "dire" : "neutral");
    }

    // --- Getters & Setters ---
    public String getName() { return name; }
    public TeamEnum getTeamEnum() { return team; }
    public double getMaxHealth() { return maxHealth; }
    public double getCurrentHealth() { return currentHealth; }
    public boolean isAlive() { return isAlive; }
    public boolean isGlyphActive() { return isGlyphActive; }
    public void setGlyphActive(boolean glyphActive) { isGlyphActive = glyphActive; }
    public Entity getEntity() { return entity; }
    public void setEntity(Entity entity) { this.entity = entity; }
    public int getGoldReward() { return goldReward; }
    public int getXpReward() { return xpReward; }
    public double getArmor() { return armor; }
}