package com.dotaCraft.Item.impl;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.IconManager;
import com.dotaCraft.Objective.Rune;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Bottle extends Item {

    private static final String BOTTLE_ICON_0 = "\uE305";
    private static final String BOTTLE_ICON_1 = "\uE306";
    private static final String BOTTLE_ICON_2 = "\uE307";
    private static final String BOTTLE_ICON_3 = "\uE308";

    public Rune.RuneType storedRune = null;

    private int currentFills = 3;
    public boolean isRuned = false;

    private static final double TOTAL_HEAL = 110.0;
    private static final double TOTAL_MANA = 60.0;
    private static final int TOTAL_TICKS = 54;

    private BukkitTask activeRegenTask;

    public Bottle() {
        super(
                "bottle",
                TargetTypes.NO_TARGET,
                675,
                1.0,
                0,
                350,
                3,
                true
        );
    }

    @Override
    public void onUseNoTarget(Player player) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        if (storedRune != null) {
            Rune.RuneType runeToActivate = storedRune;
            storedRune = null;
            refill();

            //player.getWorld().playSound(player.getLocation(), "dotacraft:items.bottle_use", 0.5f, 1.0f);
            Rune.activateRune(hero, runeToActivate);
            return;
        }

        if (currentFills <= 0) {
            // blank usage
            player.getWorld().playSound(player.getLocation(), "dotacraft:items.bottle_use", 0.5f, 1.0f);
            return;
        }

        currentFills--;

        // Sound
        player.getWorld().playSound(player.getLocation(), "dotacraft:items.bottle_use", 0.5f, 1.0f);

        IconManager.removePositiveIcon(player, BOTTLE_ICON_0, BOTTLE_ICON_1, BOTTLE_ICON_2, BOTTLE_ICON_3);

        // Icon render
        if ( getCurrentFills() == 2) {
            IconManager.spawnPositiveIcon(player, BOTTLE_ICON_2, TOTAL_TICKS);
        } else if ( getCurrentFills() == 1) {
            IconManager.spawnPositiveIcon(player, BOTTLE_ICON_1, TOTAL_TICKS);
        } else if (getCurrentFills() == 0) {
            IconManager.spawnPositiveIcon(player, BOTTLE_ICON_0, TOTAL_TICKS);
        } else {
            IconManager.spawnPositiveIcon(player, BOTTLE_ICON_3, TOTAL_TICKS);
        }

        if (activeRegenTask != null && !activeRegenTask.isCancelled()) {
            activeRegenTask.cancel();
        }

        final Hero attackerBeforeCast = hero.getLastAttacker();
        final long castTimestamp = System.currentTimeMillis();

        activeRegenTask = new BukkitRunnable() {
            private int currentTick = 0;
            private double alreadyHealed = 0.0;
            private double alreadyRestoredMana = 0.0;
            private int lastKnownHealth = hero.getCurrentHealth();

            @Override
            public void run() {
                if (player == null || !player.isOnline() || hero.isHeroDead()) {
                    cancel();
                    return;
                }

                if (hero.getCurrentHealth() < lastKnownHealth) {
                    boolean damagedByEnemyHero = hero.getLastAttacker() != null
                            && hero.getLastAttackTimestamp() >= castTimestamp;

                    // TODO: Add later: || hero.getLastRoshanAttackTimestamp() >= castTimestamp
                    if (damagedByEnemyHero) {
                        cancel();
                        return;
                    }
                }

                currentTick++;
                double progress = (double) currentTick / TOTAL_TICKS;

                // 1. Health regen
                double targetHealed = TOTAL_HEAL * progress;
                double healAmount = targetHealed - alreadyHealed;
                alreadyHealed += healAmount;
                hero.addHealth(healAmount);

                // 2. Mana regen
                double targetMana = TOTAL_MANA * progress;
                double manaAmount = targetMana - alreadyRestoredMana;
                alreadyRestoredMana += manaAmount;
                hero.addMana(manaAmount);

                lastKnownHealth = hero.getCurrentHealth();

                if (currentTick >= TOTAL_TICKS) {
                    cancel();
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
    }

    public int getCurrentFills() {
        return currentFills;
    }

    public void refill() {
        this.currentFills = 3;
    }

    public void storeRune (Rune.RuneType runeToAdd) {
        // TODO
    }
}