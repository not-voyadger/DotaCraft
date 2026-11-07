package com.dotaCraft.Hero.impl.invoker;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.Supplier;

public class Invoke extends Ability {

    private static final Map<String, Supplier<Ability>> SPELL_RECIPES = new HashMap<>();

    static {
        // QQQ - Cold Snap
        //SPELL_RECIPES.put("QQQ", ColdSnap::new);
        // QQW - Ghost Walk
        //SPELL_RECIPES.put("QQW", GhostWalk::new);
        // QQE - Ice Wall
        //SPELL_RECIPES.put("EQQ", IceWall::new);
        // WWW - EMP
        //SPELL_RECIPES.put("WWW", EMP::new);
        // WWQ - Tornado
        //SPELL_RECIPES.put("WWQ", Tornado::new);
        // WWE - Alacrity
        //SPELL_RECIPES.put("WWE", Alacrity::new);
        // EEE - Sun Strike
        //SPELL_RECIPES.put("EEE", SunStrike::new);
        // EEQ - Forge Spirit
        SPELL_RECIPES.put("EEQ", ForgeSpirits::new);
        // EEW - Chaos Meteor
        //SPELL_RECIPES.put("EEW", ChaosMeteor::new);
        // QWE - Deafening Blast
        //SPELL_RECIPES.put("QWE", DeafeningBlast::new);
    }

    public Invoke() {
        super(
                DamageTypes.NONE,
                AbilityTypes.NO_TARGET,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[] {0},
                new double[] {0},
                new double[] {0},
                new double[] {7},
                new double[] {0},
                0,
                new double[] {0},
                null,
                1,
                1,
                0,
                false,
                false,
                false
        );
    }

    @Override
    public boolean cast(Hero hero) {
        if (!(hero instanceof Invoker invoker)) {
            return false;
        }

        Queue<Ability> activeOrbs = invoker.getActiveOrbs();

        if (activeOrbs.size() < 3) {
            // cast sound
            return true;
        }

        String key = buildOrbKey(activeOrbs);
        Supplier<Ability> spellSupplier = SPELL_RECIPES.get(key);

        if (spellSupplier == null) {
            return false;
        }

        invoker.invokeSpell(spellSupplier.get());
        return true;
    }

    private String buildOrbKey(Queue<Ability> orbs) {
        return orbs.stream()
                .map(orb -> {
                    if (orb instanceof Quas) return "Q";
                    if (orb instanceof Wex) return "W";
                    if (orb instanceof Exort) return "E";
                    return "";
                })
                .sorted()
                .reduce("", String::concat);
    }
}
