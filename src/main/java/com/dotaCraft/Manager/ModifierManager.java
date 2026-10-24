package com.dotaCraft.Manager;

public class ModifierManager {
    private final String id;
    private final double healthRegen;
    private final double manaRegen;
    private final double armor;

    private final long durationTicks;
    private long ticksLived = 0;

    public ModifierManager(String id, double healthRegen, double manaRegen, double armor, double durationInSeconds) {
        this.id = id;
        this.healthRegen = healthRegen;
        this.manaRegen = manaRegen;
        this.armor = armor;
        this.durationTicks = durationInSeconds > 0 ? (long) (durationInSeconds * 20.0) : -1L;
    }

    public ModifierManager(String id, double healthRegen, double durationInSeconds) {
        this(id, healthRegen, 0.0, 0.0, durationInSeconds);
    }

    /**
     * Called every tick.
     * @return true, if modifier effect ends.
     */
    public boolean tick() {
        if (durationTicks < 0) return false;
        ticksLived++;
        return ticksLived >= durationTicks;
    }

    public String getId() { return id; }
    public double getHealthRegen() { return healthRegen; }
    public double getManaRegen() { return manaRegen; }
    public double getArmor() { return armor; }
}