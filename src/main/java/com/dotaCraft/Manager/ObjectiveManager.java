package com.dotaCraft.Manager;

import com.dotaCraft.Objective.Objective;
import com.dotaCraft.Objective.Tower;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectiveManager implements Listener {

    private final List<Objective> activeObjectives = new ArrayList<>();

    public Tower spawnTower(String name, Objective.TeamEnum team, int tier, Location location) {
        Tower tower = new Tower(name, team, tier, location);
        tower.spawn(location);
        activeObjectives.add(tower);
        return tower;
    }

    public void setupRadiantStructures(World world) {
        if (world == null) return;

        // --- TOP LANE ---
        spawnTower("Radiant Top T1", Objective.TeamEnum.RADIANT, 1, new Location(world, -272, -30, -75));
        spawnTower("Radiant Top T2", Objective.TeamEnum.RADIANT, 2, new Location(world, -272, -38, -35));
        spawnTower("Radiant Top T3", Objective.TeamEnum.RADIANT, 3, new Location(world, -273, -38, 10));

        // --- MID LANE ---
        /*spawnTower("Radiant Mid T1", Objective.TeamEnum.RADIANT, 1, new Location(world, -45.5, 64.0, -45.5));
        spawnTower("Radiant Mid T2", Objective.TeamEnum.RADIANT, 2, new Location(world, -80.5, 64.0, -10.5));
        spawnTower("Radiant Mid T3", Objective.TeamEnum.RADIANT, 3, new Location(world, -110.5, 64.0, 20.5));*/

        // --- BOT LANE ---
        spawnTower("Radiant Bot T1", Objective.TeamEnum.RADIANT, 1, new Location(world, -143, -40, 54));
        spawnTower("Radiant Bot T2", Objective.TeamEnum.RADIANT, 2, new Location(world, -183, -40, 54));
        spawnTower("Radiant Bot T3", Objective.TeamEnum.RADIANT, 3, new Location(world, -227, -39, 54));

        // --- T4 TOWERS (Ancient Protectors) ---
        spawnTower("Radiant Ancient T4 Left", Objective.TeamEnum.RADIANT, 4, new Location(world, -255, -39, 33));
        spawnTower("Radiant Ancient T4 Right", Objective.TeamEnum.RADIANT, 4, new Location(world, -251, -39, 37));
    }

    public void onTick() {
        activeObjectives.removeIf(objective -> !objective.isAlive());
        for (Objective objective : activeObjectives) {
            objective.onTick();
        }
    }

    public List<Objective> getActiveObjectives() {
        return Collections.unmodifiableList(activeObjectives);
    }

    public void clearAll() {
        for (Objective objective : activeObjectives) {
            if (objective.getEntity() != null && objective.getEntity().isValid()) {
                objective.getEntity().remove();
            }
        }
        activeObjectives.clear();
    }
}