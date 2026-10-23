package com.dotaCraft.Objective;

import org.bukkit.Location;

public class Rune extends Objective {

    final Location location;

    public Rune(Location location) {
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
    }

    @Override
    public void spawn(Location loc) {

    }

    @Override
    protected void updateNameTag() {

    }
}
