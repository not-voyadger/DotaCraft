package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.EffectManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;

import java.util.List;

public class ScytheOfVyse extends Item {

    private static final double HEX_DURATION_SECONDS = 3.5;
    private static final int MANA_COST = 250;

    public ScytheOfVyse() {
        super("scythe_of_vyse", TargetTypes.UNIT_TARGET, 700, 20.0, MANA_COST, 800.0, 0, false);

        addStatBonus(StatType.INTELLECT, 30.0);
        addStatBonus(StatType.MANA_REGEN, 8.5);

        setRecipeComponents(List.of("mystic_stuff", "tiara_of_selemene", "recipe_scythe_of_vyse"));
    }

    @Override
    public void onUseUnitTarget(Player player, Entity target) {
        if (!(target instanceof LivingEntity targetEntity) || targetEntity.isDead()) {
            return;
        }

        EffectManager.applyHex(
                targetEntity,
                HEX_DURATION_SECONDS,
                Pig.class,
                "dotacraft:scythe_of_vyse.scythe_of_vyse_cast",
                "dotacraft:scythe_of_vyse.scythe_of_vyse_return"
        );
    }
}