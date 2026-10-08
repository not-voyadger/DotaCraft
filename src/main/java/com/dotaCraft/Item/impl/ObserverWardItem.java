package com.dotaCraft.Item.impl;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ObserverWardItem extends Item {

    public ObserverWardItem() {
        // id, targetType, cost, cooldown, manaCost, castRange, maxCharges, isConsumable
        super("observer_ward", TargetTypes.POINT_TARGET, 0, 1.0, 0.0, 500.0, 4, true);
    }

    @Override
    public void onUsePointTarget(Player player, Location targetLocation) {
        Hero hero = com.dotaCraft.Manager.HeroManager.getHero(player);
        if (hero == null) return;

        int slot = hero.getSlotOfItem(this);
        if (slot == -1) return;

        var targetBlock = player.getTargetBlockExact((int) getCastRange());
        Location spawnLoc;
        if (targetBlock != null) {
            spawnLoc = targetBlock.getLocation().add(0.5, 1.0, 0.5);
        } else {
            spawnLoc = targetLocation;
        }

        DotaCraft.getInstance().getWardManager().placeObserverWard(hero, targetLocation);

        player.getWorld().playSound(targetLocation, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.2f);
        //player.getWorld().playSound(targetLocation, "dotacraft:items.ward_place", 0.8f, 1.0f);

        hero.removeItemFromSlot(slot);
    }
}