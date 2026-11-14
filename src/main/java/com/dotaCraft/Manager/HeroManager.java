package com.dotaCraft.Manager;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Hero.impl.crystal_maiden.CrystalMaiden;
import com.dotaCraft.Hero.impl.invoker.Invoker;
import com.dotaCraft.Hero.impl.lion.Lion;
import com.dotaCraft.Hero.impl.pudge.Pudge;
import com.dotaCraft.Hero.impl.shadow_fiend.ShadowFiend;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class HeroManager {

    public HeroManager(HeroBarManager heroBarManager) {
        this.heroBarManager = heroBarManager;
    }

    private static final Map activeHeroes = new HashMap<>();

    private static final Map heroFactories = new HashMap<>();

    private HeroBarManager heroBarManager;

    static {
        registerHeroType("pudge", player -> new Pudge((Player) player));
        registerHeroType("invoker", player -> new Invoker((Player) player));
        registerHeroType("lion", player -> new Lion((Player) player));
        registerHeroType("crystal_maiden", player -> new CrystalMaiden((Player) player));
        registerHeroType("shadow_fiend", player -> new ShadowFiend((Player) player));
    }

    public static void registerHeroType(String id, Function factory) {
        heroFactories.put(id.toLowerCase(), factory);
    }

    public Hero createAndRegisterHero(String heroId, Player player) {
        Function factory = (Function) heroFactories.get(heroId.toLowerCase());
        if (factory == null) return null;

        unregisterHero(player);

        IconManager.clearPlayer(player);

        Hero hero = (Hero) factory.apply(player);
        registerHero(player, hero);

        hero.updateSpeedAttribute();

        if (heroBarManager != null) {
            heroBarManager.createBars(player, hero);
        }
        return hero;
    }

    public static void notifyUnitDeath(LivingEntity victim, Hero killer) {
        if (victim == null) return;

        for (Object obj : activeHeroes.values()) {
            Hero hero = (Hero) obj;
            Player player = hero.getPlayer();
            if (player == null || !player.isOnline()) continue;

            double distance = player.getWorld().equals(victim.getWorld())
                    ? player.getLocation().distance(victim.getLocation())
                    : Double.MAX_VALUE;

            Ability innate = hero.getInnateAbility();
            if (innate != null && innate.getAbilityLevel() > 0) {
                innate.onUnitDeath(hero, victim, killer, distance);
            }

            for (int i = 0; i < 6; i++) {
                Ability ability = hero.getAbilityInSlot(i);
                if (ability != null && ability.getAbilityLevel() > 0) {
                    ability.onUnitDeath(hero, victim, killer, distance);
                }
            }
        }
    }

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

    public static Set getAvailableHeroIds() {
        return heroFactories.keySet();
    }

    public void clear() {
        activeHeroes.clear();
    }
}