package com.dotaCraft.Manager;

import com.dotaCraft.Hero.Hero;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    public enum Runes {DOUBLE_DAMAGE, WATER, HASTE, }; // TO DO

    private List radiantHeroes = new ArrayList();
    private List direHeroes = new ArrayList();
    public List availableHeroes = new ArrayList();

    public void randomizePlayers(List players) {
        // TO DO
    }

    public void getTimeForNextRoshan() {
        // TO DO
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

    public List getRadiantHeroes() {
        return radiantHeroes;
    }

    public List getDireHeroes() {
        return direHeroes;
    }
}
