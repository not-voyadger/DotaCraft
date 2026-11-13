package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;

public class BladesOfAttack extends Item {
    public BladesOfAttack() {
        super("blades_of_attack", TargetTypes.NO_TARGET, 450, 0, 0, 0, 0, false, false);

        addStatBonus(StatType.DAMAGE, 9.0);
    }
}
