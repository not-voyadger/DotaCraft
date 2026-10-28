package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.entity.Player;

public class HealingSalve extends Item {

    private static final String HEALING_SALVE_ICON = "\uE304";
    private static final double DURATION_TICKS = 25.0 * 20.0;

    public HealingSalve() {
        super(
                "Healing Salve",
                TargetTypes.NO_TARGET,
                100,
                120,
                0,
                250.0,
                0,
                true
        );
    }

    @Override
    public void onUseNoTarget(Player player) {
        if (player == null || !player.isOnline()) return;

        player.getWorld().playSound(player.getLocation(), "dotacraft:items.healing_salve", 0.2f, 1.0f);

        IconManager.spawnPositiveIcon(player, HEALING_SALVE_ICON, DURATION_TICKS);
    }
}
