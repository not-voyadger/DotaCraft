package com.dotaCraft.Hero.impl.shadow_fiend;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;

public class PresenceOfTheDarkLord extends Ability {

    private double[] reducedArmor = {2.5, 4, 5.5, 7};
    public static final String ICON = ""; // TODO

    public PresenceOfTheDarkLord() {
        super(
                DamageTypes.NONE,
                AbilityTypes.AURA,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[] {0, 0, 0, 0},
                new double[] {0, 0, 0, 0},
                new double[] {0, 0, 0, 0},
                new double[] {0, 0, 0, 0},
                new double[] {0, 0, 0, 0},
                0,
                new double[] {1200, 1200, 1200, 1200}, //range
                new double[] {20, 20, 20, 20}, //stack duration
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
        return false;
    }

    @Override
    public void onTick(Hero hero) {

    }
}
