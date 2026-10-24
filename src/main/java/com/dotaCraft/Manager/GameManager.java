package com.dotaCraft.Manager;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Objective.Rune;
import org.bukkit.World;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {
    private enum GameState {WAITING, DRAFT, PRE_GAME, GAME, POST_GAME};

    private List radiantHeroes = new ArrayList();
    private List direHeroes = new ArrayList();
    public List availableHeroes = new ArrayList();
    List spawnedRunes = new ArrayList();
    private static GameManager instance;
    private GameState gameState = GameState.WAITING;

    public void changeGameState(GameState gameState) {
        // TODO
    }

    public void randomizePlayers(List players) {
        // TODO
    }

    public void getTimeForNextRoshan() {
        // TODO
    }

    public void spawnMidRune() {
        World world = null; // Temp
        Random random = new Random();

        Rune.RuneType runeType = getNextRune(spawnedRunes);

        Location topRuneSpot = new Location(world, 0, 0, 0); //temp
        Location bottomRuneSpot = new Location(world, 1, 1, 1); //temp

        Location loc;
        if (random.nextBoolean()) {
            loc = topRuneSpot;
        } else {
            loc = bottomRuneSpot;
        }

        Rune rune = Rune.spawn(loc, runeType);
        spawnedRunes.add(rune);
    }

    public Rune.RuneType getNextRune(List<Rune> spawnedRunes) {
        Random random = new Random();
        Rune.RuneType[] types = Rune.RuneType.values();

        Rune.RuneType nextType = types[random.nextInt(types.length)];

        if (!spawnedRunes.isEmpty()) {
            Rune lastRune = spawnedRunes.get(spawnedRunes.size() - 1);

            while (nextType == lastRune.getType()) {
                nextType = types[random.nextInt(types.length)];
            }
        }

        return nextType;
    }

    public void assignPlayerToHero(Player player, Hero hero) {
        if (hero == null || player == null) {
            return;
        }

        hero.setPlayer(player);
    }

    public void addHeroToRadiant(Hero hero) {
        if (hero == null) {
            return;
        }
        radiantHeroes.add(hero);
    }

    public void addHeroToDire(Hero hero) {
        if (hero == null) {
            return;
        }
        direHeroes.add(hero);
    }

    public void markHeroAsBanned(Hero hero) {
        // TO DO

        availableHeroes.remove(hero);
    }

    public void markHeroAsPicked(Hero hero) {
        // TO DO

        availableHeroes.remove(hero);
    }

    public List<Hero> getRadiantHeroes() {
        return radiantHeroes;
    }

    public List<Hero> getDireHeroes() {
        return direHeroes;
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
}
