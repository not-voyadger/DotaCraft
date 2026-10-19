package com.dotaCraft.Hero.impl.silencer;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.EffectManager;
import com.dotaCraft.Manager.GameManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class GlobalSilence extends Ability {
    public GlobalSilence() {
        super(
                DamageTypes.PHYSICAL,
                AbilityTypes.NO_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.BASIC,
                new double []{0,0,0},
                new double []{300,450,600},
                new double []{0,0,0},
                new double []{130,115,100},
                new double []{0,0,0},
                0.67,
                new double []{0,0,0},
                new double []{4.5, 5.25, 6},
                0,
                3,
                6,
                false,
                true,
                false
        );
    }

    @Override
    public boolean cast(Hero hero) {
        String silencerTeam = hero.getTeam();

        if (silencerTeam == "radiant") {
            List<Hero> direHeroes = GameManager.getInstance().getDireHeroes();

            for (Hero enemy : direHeroes) {
                EffectManager.applySilence(enemy, getDuration(getAbilityLevel()));
            }
        } else {
            List<Hero> radiantHeroes = GameManager.getInstance().getRadiantHeroes();
        }



        return true;
    }
}
