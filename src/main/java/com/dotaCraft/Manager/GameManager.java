package com.dotaCraft.Manager;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Objective.Rune;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {
    public enum Runes {DOUBLE_DAMAGE, HASTE, SHIELD, REGENERATION, INVISIBILITY, ILLUSION, ARCANE};

    private List radiantHeroes = new ArrayList();
    private List direHeroes = new ArrayList();
    public List availableHeroes = new ArrayList();
    private static GameManager instance;

    public void randomizePlayers(List players) {
        // TO DO
    }

    public void getTimeForNextRoshan() {
        // TO DO
    }

    public void spawnMidRune() {
        Random random = new Random();
        List spawnedRunes = new ArrayList();

        getNextRune(spawnedRunes);

        int randomVal = random.nextInt(2);

        if (randomVal == 1 ) {
            // spawn top tune
        } else {
            // spawn bottom rune
        }

    }

    public void getNextRune(List spawnedRunes) {
        // TO DO
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
