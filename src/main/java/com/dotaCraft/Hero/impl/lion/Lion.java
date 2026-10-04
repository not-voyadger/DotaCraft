package com.dotaCraft.Hero.impl.lion;

import com.dotaCraft.Hero.Hero;
import org.bukkit.entity.Player;

public class Lion extends Hero {
    public Lion(Player player) {
        super(player, "Lion", Attribute.INTELLECT, 18, 15, 19, 2.3, 0.9, 2.4, 1.7, 3.5, 1.7, AttackType.RANGED, 600.0, 900.0, 29, 35);

        this.addAbility(0, new EarthSpike());
        this.addAbility(1, new Hex());
    }
}
