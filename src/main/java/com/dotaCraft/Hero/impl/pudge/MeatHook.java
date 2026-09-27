package com.dotaCraft.Hero.impl.pudge;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;

public class MeatHook extends Ability {

    public MeatHook() {
        super(
                DamageTypes.PURE,
                AbilityTypes.POINT_TARGET,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[]{150, 220, 290, 360}, //damage
                new double[]{120,120,120,120}, //manaCost
                new double[]{0,0,0,0}, // healthCost
                new double[]{18,16,14,12}, // coolDown
                new double[]{1300,1300,1300,1300}, // castRange
                0, // castPoint
                new double[]{0,0,0,0}, // effectRadius
                new double[]{2,2,2,2}, // duration
                1,
                4,
                1,
                false, //isInnate
                false, //hasScepterUpgrade
                true //hasShardUpgrade
        );
    }

    public void cast(Hero hero) {
        hero.getPlayer().sendMessage("MeatHook.");
    }
}
