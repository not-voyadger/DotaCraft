package com.dotaCraft.Manager;

import com.dotaCraft.Hero.Hero;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class HeroManager {
    private static final Map activeHeroes = new HashMap<>();

    public void registerHero(Player player, Hero hero) {
        activeHeroes.put(player.getUniqueId(), hero);
    }

    public void unregisterHero(Player player) {
        activeHeroes.remove(player.getUniqueId());
    }

    public static Hero getHero(Player player) {
        if (player == null) return null;
        return (Hero) activeHeroes.get(player.getUniqueId());
    }

    public boolean hasHero(Player player) {
        return activeHeroes.containsKey(player.getUniqueId());
    }

    public void clear() {
        activeHeroes.clear();
    }
}