package com.dotaCraft.Item.impl;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class EnchantedMango extends Item {
    public EnchantedMango() {
        super("enchanted_mango", TargetTypes.NO_TARGET, 65, 0, 0, 400.0, 0, true);

        addStatBonus(StatType.MANA_REGEN, 0.4);
    }

    @Override
    public void onUseNoTarget(Player player) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        int slot = hero.getSlotOfItem(this);
        if (slot == -1) return;

        hero.addMana(100);

        //player.getWorld().playSound(player.getLocation(), "dotacraft:items.enchanted_mango_consume", 1.0f, 1.2f);

        hero.removeItemFromSlot(slot);
    }

    @Override
    public void onUseUnitTarget(Player player, Entity target) {
        //TO DO: player can feed mango to his teammate with Ctrl+slot bind
    }
}
