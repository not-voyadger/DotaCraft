package com.dotaCraft.Hero.impl.invoker;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Hero.impl.pudge.FleshHeap;
import org.bukkit.entity.Player;

public class Invoker extends Hero {
    public Invoker(Player player) {
        super(player, "Invoker", Attribute.INTELLECT, 19, 14, 22, 2.15, 1.1, 2.5, 2.0, 4.0, 1.7, AttackType.RANGED, 600.0, 900.0, 20, 26);

        this.addAbility(1, new ForgeSpirits());

    }
}
