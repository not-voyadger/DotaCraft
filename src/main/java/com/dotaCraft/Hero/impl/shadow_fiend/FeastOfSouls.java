package com.dotaCraft.Hero.impl.shadow_fiend;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class FeastOfSouls extends Ability {

    private final double[] additionalAttackSpeed = {35, 50, 65, 80};
    private final double[] additionalMoveSpeedInPercent = {4, 6, 8, 10};
    private final double[] maxVictimCount = {4, 6, 8, 10};
    private final int soulsFromHero = 3;
    private final int soulsFromCreep = 1;
    public static final String ICON = ""; // TODO
    public static final String EFFECT_ICON = "\uE507";

    public FeastOfSouls() {
        super(
                DamageTypes.NONE,
                AbilityTypes.NO_TARGET,
                TargetTypes.NONE,
                DispelTypes.BASIC,
                new double[] {0, 0, 0, 0},
                new double[] {60, 65, 70, 75},
                new double[] {0, 0, 0, 0},
                new double[] {21, 21, 21, 21},
                new double[] {0, 0, 0, 0},
                0,
                new double[] {0, 0, 0, 0},
                new double[] {8, 8, 8, 8},
                1,
                4,
                0,
                false,
                false,
                false,
                ICON
        );
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();
        int levelIndex = Math.max(0, Math.min(getAbilityLevel() - 1, 3));

        double bonusAS = additionalAttackSpeed[levelIndex];
        double bonusMSPercent = additionalMoveSpeedInPercent[levelIndex] / 100.0;
        int durationTicks = 160;

        IconManager.spawnPositiveIcon(player, EFFECT_ICON, durationTicks);
        // TODO: add sound here.

        hero.addBonusAttackSpeed(bonusAS);
        hero.addBonusMoveSpeedPercent(bonusMSPercent);

        new BukkitRunnable() {
            private int currentTick = 0;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || hero.isHeroDead() || currentTick >= durationTicks) {
                    hero.addBonusAttackSpeed(-bonusAS);
                    hero.addBonusMoveSpeedPercent(-bonusMSPercent);
                    cancel();
                    return;
                }
                currentTick++;
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);

        return true;
    }
}