package com.dotaCraft.Item.impl;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.entity.Player;

public class MagicStick extends Item {

    public MagicStick() {
        // id, targetType, cost, cooldown, manaCost, castRange, maxCharges, isConsumable
        super("magic_stick", TargetTypes.NO_TARGET, 200, 13.0, 0.0, 0.0, 10, false);
    }

    @Override
    public void onUseNoTarget(Player player) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        int currentCharges = getCurrentCharges();

        if (currentCharges > 0) {
            double manaToAdd = currentCharges * 15.0;
            double healthToAdd = currentCharges * 15.0;

            // adding mana/health
            hero.addMana(manaToAdd);
            hero.addHealth(healthToAdd);

            currentCharges = 0;
        } else {
            // TO DO: logic to refuse adding mana/health, sound effect
            player.sendMessage("§cNo charges!");
        }
    }
}