package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;

import java.util.List;

public class Daedalus extends Item {
    public Daedalus() {
        super("daedalus", TargetTypes.NO_TARGET, 0, 0, 0, 0, 0, false);

        addStatBonus(StatType.DAMAGE, 88.0);
        addStatBonus(StatType.CRIT_CHANCE, 30.0);
        addStatBonus(StatType.CRIT_MULTIPLIER, 2.25);

        setRecipeComponents(List.of("demon_edge", "crystalys", "recipe_daedalus"));
    }


}
