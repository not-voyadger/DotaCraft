package com.dotaCraft.Hero.impl.axe;

import com.dotaCraft.Hero.Hero;
import org.bukkit.entity.Player;

public class Axe extends Hero {
    public Axe(Player player) {
        super(
                player,
                "axe",
                Attribute.STRENGTH,
                25,
                18,
                18,
                4.5,
                0.9,
                2.7,
                1.5,
                1.6,
                1.7,
                AttackType.MELEE,
                150,
                0.0,
                31,
                35,
                315
        );

        addAbility(1, new BattleHunger());
        addAbility(3, new CullingBlade());
    }
}
