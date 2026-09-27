package com.dotaCraft.Hero.impl.pudge;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;

public class FleshHeap extends Ability {

    private int stackCount = 0;

    public FleshHeap() {
        super(
                DamageTypes.NONE,
                AbilityTypes.PASSIVE,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[]{0,0,0,0},
                new double[]{0,0,0,0},
                new double[]{0,0,0,0},
                new double[]{0,0,0,0},
                new double[]{0,0,0,0},
                0,
                new double[]{0,0,0,0},
                new double[]{0,0,0,0},
                1,
                1,
                1,
                true,  // isInnate = true
                false, // hasScepterUpgrade
                false  // hasShardUpgrade
        );
    }

    @Override
    public void onEnemyDeath(Hero Pudge, Hero victim, double radius) {
        if (Pudge.equals(victim.getLastAttacker()) || radius <= 450.0) {
            stackCount++;
            Pudge.addStrength(2);
        }
    }

    public void cast(Hero hero) {
        hero.getPlayer().sendMessage("FleshHeap.");
    }
}
