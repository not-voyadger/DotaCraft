package com.dotaCraft.Item.impl;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;

public class MagicStick extends Item {

    public MagicStick() {
        // id, targetType, cost, cooldown, manaCost, castRange, maxCharges, isConsumable
        super("magic_stick", TargetTypes.NO_TARGET, 200, 13.0, 0.0, 0.0, 10, false);
    }

    @Override
    public void onUseNoTarget(org.bukkit.entity.Player player) {
        int currentCharges = getCurrentCharges();

        if (currentCharges != 0) {
            int manaToAdd = currentCharges * 15;
            int healthToAdd = currentCharges * 15;

            int currentMana = Hero.getCurrentMana();
            int currentHealth = Hero.getCurrentHealth();

            currentMana += manaToAdd;
            currentHealth += healthToAdd;

            currentCharges = 0;
        } else {
            // TO DO: logic to refuse adding mana/health, sound effect
        }
    }
}