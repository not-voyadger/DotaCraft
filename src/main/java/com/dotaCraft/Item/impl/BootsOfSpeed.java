package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;

public class BootsOfSpeed extends Item {
    public BootsOfSpeed() {
        super("boots_of_speed", TargetTypes.NO_TARGET, 500, 0, 0, 0, 0, false);

        addStatBonus(StatType.MOVEMENT_SPEED, 45);
    }
}
