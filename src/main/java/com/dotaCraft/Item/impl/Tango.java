package com.dotaCraft.Item.impl;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.IconManager;
import com.dotaCraft.Manager.ModifierManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Tango extends Item {

    public Tango() {
        super("tango", TargetTypes.POINT_TARGET, 90, 4.0, 0.0, 200.0, 3, true);
    }

    @Override
    public void onUsePointTarget(Player player, Location targetLocation) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        Block targetBlock = player.getTargetBlockExact((int) getCastRange());
        if (targetBlock == null) return;

        var treeManager = DotaCraft.getInstance().getTreeManager();

        if (treeManager.isTreeBlock(targetBlock)) {
            treeManager.destroyTree(targetBlock);

            double duration = treeManager.isPlantedByPlayer() ? 32.0 : 16.0;
            double durationInTicks = duration * 20.0;

            player.getWorld().playSound(player.getLocation(), "dotacraft:items.tango_consume", 0.2f, 1.0f);

            IconManager.spawnPositiveIcon(player, "\uE300", durationInTicks);

            hero.addModifier(new ModifierManager("tango_regen", 7.0, duration));

            int slot = hero.getSlotOfItem(this);
            if (slot != -1) {
                hero.removeItemFromSlot(slot);
            }
        } else {
            // TODO: error sound here.
        }
    }
}