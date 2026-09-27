package com.dotaCraft.Hero.impl.pudge;

import com.dotaCraft.Hero.Hero;
import org.bukkit.entity.Player;

public class Pudge extends Hero {
    public Pudge(Player player) {
        super(player, "Pudge", Attribute.STRENGTH, 30.0, 14.0, 16.0, 3.0, 1.4, 1.8);

        this.addAbility(0, new FleshHeap());
    }
}