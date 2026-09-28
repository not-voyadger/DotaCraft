package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;

public class IronBranch extends Item {

    public IronBranch() {
        super("iron_branch", TargetTypes.POINT_TARGET, 50, 0.0, 0.0, 200.0, 0, true);

        // Adding stats
        addStatBonus(StatType.STRENGTH, 1.0);
        addStatBonus(StatType.AGILITY, 1.0);
        addStatBonus(StatType.INTELLECT, 1.0);
    }

    @Override
    public void onUsePointTarget(org.bukkit.entity.Player player, org.bukkit.Location targetLocation) {
        // TO DO: tree creation logic
    }
}