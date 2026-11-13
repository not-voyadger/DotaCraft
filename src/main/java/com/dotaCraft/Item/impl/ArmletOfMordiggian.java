package com.dotaCraft.Item.impl;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ArmletOfMordiggian extends Item {

    private static final String ARMLET_ICON = "\uE309";
    private final Set<UUID> activeUsers = new HashSet<>();

    public ArmletOfMordiggian() {
        super(
                "armlet_of_mordiggian",
                TargetTypes.TOGGLE,
                0,
                0,
                0,
                0,
                0,
                false,
                true
        );

        addStatBonus(StatType.HEALTH_REGEN, 5);
        addStatBonus(StatType.ARMOR, 6);
        addStatBonus(StatType.DAMAGE, 15);
        addStatBonus(StatType.ATTACK_SPEED, 25);

        setRecipeComponents(List.of("helm_of_iron_will", "gloves_of_haste", "blades_of_attack", "recipe_armlet"));
    }

    // FIXME: REALLY bad!!! Infinite damage addition. WTF?!
    @Override
    public void onToggle(Player player, boolean active) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        UUID uuid = player.getUniqueId();

        if (active) {
            if (activeUsers.add(uuid)) {
                IconManager.spawnPositiveIcon(player, ARMLET_ICON);
                hero.setMainDamage(hero.getMainDamage() + 35);
                // TODO: +25 strength, +4 armor
                startArmletCycle(player);
            }
        } else {
            if (activeUsers.remove(uuid)) {
                hero.setMainDamage(hero.getMainDamage() - 35);
                // TODO: -25 strength, -4 armor
                IconManager.removePositiveIcon(player, ARMLET_ICON);
            }
        }

        player.sendMessage("[Debug]: Current damage: " + hero.getMainDamage());
    }

    private void startArmletCycle(Player player) {
        // TODO
    }
}
