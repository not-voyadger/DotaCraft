package com.dotaCraft.Item.impl;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.HologramManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class FaerieFire extends Item {

    private final double healthToAdd = 85;

    public FaerieFire() {
        super(
                "faerie_fire",
                TargetTypes.NO_TARGET,
                65,
                5,
                0,
                0,
                1,
                true
        );

        addStatBonus(StatType.DAMAGE, 2.0);
    }

    @Override
    public void onUseNoTarget(Player player) {
        Hero hero = HeroManager.getHero(player);

        hero.removeItemFromSlot(hero.getSlotOfItem(this));

        hero.addHealth(healthToAdd);
        player.getWorld().playSound(player.getLocation(), "dotacraft:items.faerie_fire_use", 0.3f, 1.0f);
        HologramManager.spawnHealthRegenerationIndicator(hero, healthToAdd);
        spawnEffect(player);

    }

    private void spawnEffect(Player player) {
        Location loc = player.getLocation().add(0, 1.0, 0);

        player.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                loc,
                30,
                0.5, 0.8, 0.5,
                0
        );
    }
}
