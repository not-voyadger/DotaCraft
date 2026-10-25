package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.entity.Player;

public class Clarity extends Item {

    private static final String CLARITY_ICON = "\uE302";
    private static final double DURATION_TICKS = 25.0 * 20.0;

    public Clarity() {
        super(
                "clarity",
                TargetTypes.NO_TARGET,
                50,
                0.0,
                0,
                250.0,
                0,
                true
        );
    }

    @Override
    public void onUseNoTarget(Player player) {
        if (player == null || !player.isOnline()) return;

        //player.getWorld().playSound(player.getLocation(), "dotacraft:items.clarity", 0.5f, 1.0f);

        IconManager.spawnPositiveIcon(player, CLARITY_ICON, DURATION_TICKS);
    }
}