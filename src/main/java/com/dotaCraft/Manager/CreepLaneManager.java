package com.dotaCraft.Manager;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Units.LaneCreep;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class CreepLaneManager {

    private final DotaCraft plugin;
    private final List<LaneCreep> activeLaneCreeps = new ArrayList<>();
    private final Map<String, List<Location>> radiantWaypoints = new HashMap<>();

    private int waveCounter = 0;
    private BukkitTask waveTask;

    public CreepLaneManager(DotaCraft plugin) {
        this.plugin = plugin;
    }

    public void setupRadiantWaypoints(World world) {
        radiantWaypoints.clear();

        // TOP LANE
        radiantWaypoints.put("TOP", List.of(
                new Location(world, -255, -39, 33),  // Radiant
                new Location(world, -273, -38, 10),  // Radiant Top T3
                new Location(world, -272, -38, -35), // Radiant Top T2
                new Location(world, -272, -30, -75)  // Radiant Top T1
        ));

        // BOT LANE
        radiantWaypoints.put("BOT", List.of(
                new Location(world, -251, -39, 37),  // Radiant
                new Location(world, -227, -39, 54),  // Radiant Bot T3
                new Location(world, -183, -40, 54),  // Radiant Bot T2
                new Location(world, -143, -40, 54)   // Radiant Bot T1
        ));

        // MID LANE
    }

    public void startSpawning(World world) {
        stopSpawning();
        setupRadiantWaypoints(world);

        waveTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnWave();
            }
        }.runTaskTimer(plugin, 0L, 600L);
    }

    public void stopSpawning() {
        if (waveTask != null) {
            waveTask.cancel();
            waveTask = null;
        }
        clearAll();
    }

    public void spawnWave() {
        waveCounter++;
        boolean spawnSiege = (waveCounter % 10 == 0);

        for (Map.Entry<String, List<Location>> entry : radiantWaypoints.entrySet()) {
            List<Location> points = entry.getValue();
            Location spawnLoc = points.get(0);

            for (int i = 0; i < 3; i++) {
                spawnCreep(LaneCreep.CreepType.MELEE, spawnLoc, points);
            }

            spawnCreep(LaneCreep.CreepType.RANGED, spawnLoc, points);

            if (spawnSiege) {
                spawnCreep(LaneCreep.CreepType.SIEGE, spawnLoc, points);
            }
        }
    }

    private void spawnCreep(LaneCreep.CreepType type, Location spawnLoc, List<Location> waypoints) {
        LaneCreep creep = new LaneCreep(type, "radiant", spawnLoc, waypoints);
        creep.spawn(spawnLoc);
        activeLaneCreeps.add(creep);
    }

    public void onTick() {
        activeLaneCreeps.removeIf(creep -> {
            if (creep.isDead()) return true;
            creep.onTick();
            return false;
        });
    }

    public List<LaneCreep> getActiveLaneCreeps() {
        return activeLaneCreeps;
    }

    public void clearAll() {
        for (LaneCreep creep : activeLaneCreeps) {
            if (creep.getEntity() != null && creep.getEntity().isValid()) {
                creep.getEntity().remove();
            }
        }
        activeLaneCreeps.clear();
    }
}