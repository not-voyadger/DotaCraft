package com.dotaCraft.Manager;

import com.dotaCraft.Units.NeutralCreep;
import com.dotaCraft.DotaCraft;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class NeutralManager {

    private final Map creepRegistry = new HashMap<>();
    private final Map activeCreepsMap = new HashMap<>();
    private final List camps = new ArrayList<>();

    public NeutralManager() {
        registerDefaultCreeps();
        //startMinuteSpawnTimer();
    }

    private void registerDefaultCreeps() {
        registerCreep(new NeutralCreep("kobold", "Kobold Taskmaster", EntityType.ZOMBIE, 240, 12, 18, 25));
        registerCreep(new NeutralCreep("centaur", "Centaur Conqueror", EntityType.POLAR_BEAR, 1100, 55, 60, 95));
        registerCreep(new NeutralCreep("satyr", "Satyr Tormenter", EntityType.WITHER_SKELETON, 1100, 50, 62, 95));
    }

    public void registerCreep(NeutralCreep creep) {
        creepRegistry.put(creep.getId().toLowerCase(), creep);
    }

    public NeutralCreep getCreepTemplate(String id) {
        return (NeutralCreep) creepRegistry.get(id.toLowerCase());
    }

    public NeutralCreep getActiveCreep(UUID entityUuid) {
        return (NeutralCreep) activeCreepsMap.get(entityUuid);
    }

    public void removeActiveCreep(UUID entityUuid) {
        activeCreepsMap.remove(entityUuid);
    }

    public LivingEntity spawnCreepManual(String creepId, Location location) {
        NeutralCreep template = getCreepTemplate(creepId);
        if (template == null) return null;

        NeutralCreep creep = new NeutralCreep(
                template.getId(),
                template.getName(),
                template.getEntityType(),
                template.getMaxHealth(),
                template.getDamage(),
                template.getGoldReward(),
                template.getXpReward()
        );
        LivingEntity entity = creep.spawn(location);
        if (entity != null) {
            activeCreepsMap.put(entity.getUniqueId(), creep);
        }
        return entity;
    }

//    private void startMinuteSpawnTimer() {
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                for (NeutralCamp camp : camps) {
//                    List wave = List.of(getCreepTemplate("kobold"));
//                    camp.trySpawnWave(wave);
//                }
//            }
//        }.runTaskTimer(DotaCraft.getInstance(), 1200L, 1200L);
//    }

    public Set getRegisteredCreepIds() {
        return creepRegistry.keySet();
    }
}