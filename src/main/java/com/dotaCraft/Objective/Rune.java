package com.dotaCraft.Objective;

import org.bukkit.Location;

public class Rune extends Objective {

    public enum RuneType {DOUBLE_DAMAGE, HASTE, SHIELD, REGENERATION, INVISIBILITY, ILLUSION, ARCANE};

    final Location location;
    private final RuneType type;

    public Rune(Location location, RuneType type) {
        super(
                "rune",
                null,
                0,
                0,
                0,
                0,
                0,
                location,
                0
        );
        this.location = location;
        this.type = type;
    }

    @Override
    public void spawn(Location loc) {
        // TODO: spawn logic
    }

    public static Rune spawn(Location loc, RuneType type) {
        Rune rune = new Rune(loc, type);
        return rune;
    }

    public RuneType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    protected void updateNameTag() {

    }

    private void claimRune(Rune rune) {
        // TODO
    }
}
