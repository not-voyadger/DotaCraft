package com.dotaCraft.Hero.impl.invoker;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.entity.Player;

public class Quas extends Ability {

    public static final String ICON = ""; //TODO

    public Quas() {
        super(
                DamageTypes.NONE,
                AbilityTypes.NO_TARGET,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[] {0, 0, 0, 0},
                new double[] {0, 0, 0, 0},
                new double[] {0, 0, 0, 0},
                new double[] {0, 0, 0, 0},
                new double[] {0, 0, 0, 0},
                0,
                new double[] {0, 0, 0, 0},
                null,
                1,
                7,
                0,
                false,
                true,
                true,
                ICON
        );
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();

        if (hero instanceof Invoker invoker) {
            invoker.addOrb(this);
        }

        return true;
    }
}
