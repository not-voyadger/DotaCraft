package com.dotaCraft.Hero.impl.invoker;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.entity.Player;

public class Exort extends Ability {

    private double[] damageGainByOrbByLevel = {2, 4, 6, 8, 10, 12, 14, 16};
    private static final String EXORT_ICON = "\uE403";

    public Exort() {
        super(
                DamageTypes.NONE,
                AbilityTypes.NO_TARGET,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[] {0, 0, 0, 0}, // damage
                new double[] {0, 0, 0, 0}, // maan cost
                new double[] {0, 0, 0, 0}, // health cost
                new double[] {0, 0, 0, 0}, // cooldown
                new double[] {0, 0, 0, 0}, // cast range
                0,                         // cast point
                new double[] {0, 0, 0, 0}, // effect radius
                null,                      // duration
                1,                         // ability level
                8,                         // max level by default is 7, but is 8 because of aghanim upgrade
                0,                         // required level
                false,                     // isInnate?
                true,                      // hasScepterUpgrade?
                true                       // hasShardUpgrade?
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

    public double getCurrentHandDamageBonus() {
        double currentAbilityLevel = getAbilityLevel();

        if (currentAbilityLevel > 0 && currentAbilityLevel <= damageGainByOrbByLevel.length) {
            return damageGainByOrbByLevel[(int) (currentAbilityLevel - 1)];
        }

        return 0;
    }

    public void updateStats(Hero hero) {
        hero.setMainDamage(hero.getMainDamage() + getCurrentHandDamageBonus());
    }

    public void clearStats(Hero hero) {
        hero.setMainDamage(hero.getMainDamage() - getCurrentHandDamageBonus());
    }
}
