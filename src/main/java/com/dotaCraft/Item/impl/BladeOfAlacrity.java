package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;

public class BladeOfAlacrity extends Item {
    public BladeOfAlacrity() {
        super(
                "blade_of_alacrity",
                TargetTypes.NO_TARGET,
                1000,
                0,
                0,
                0,
                0,
                false
        );

        addStatBonus(StatType.AGILITY, 10.0);
    }
}
