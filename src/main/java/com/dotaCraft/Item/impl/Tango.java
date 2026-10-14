package com.dotaCraft.Item.impl;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 16 * 20, 1, false, false));
            player.sendMessage("§a[Tango] Вы съели дерево и восстанавливаете здоровье!");

            int slot = hero.getSlotOfItem(this);
            if (slot != -1) {
                hero.removeItemFromSlot(slot);
            }
        } else {
            player.sendMessage("§cНеобходимо выбрать блок дерева!");
        }
    }
}