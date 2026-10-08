package com.dotaCraft.Manager;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Objective.Ward.ObserverWard;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WardManager {

    public static class Ward {
        private final UUID wardId;
        private final Location location;
        private final double visionRadius;
        private final long expireTimestamp;
        private final String team;
        private ItemDisplay entity;
        private boolean active = true;

        public Ward(Location location, double visionRadius, int durationSeconds, String team) {
            this.wardId = UUID.randomUUID();
            this.location = location;
            this.visionRadius = visionRadius;
            this.expireTimestamp = System.currentTimeMillis() + (durationSeconds * 1000L);
            this.team = team;
        }

        public void spawn() {
            if (location.getWorld() == null) return;

            Location spawnLoc = location.clone();
            spawnLoc.setX(Math.floor(spawnLoc.getX()) + 0.5);
            spawnLoc.setZ(Math.floor(spawnLoc.getZ()) + 0.5);

            spawnLoc.add(0, 1.5, 0); //DO NOT CHANGE!!!

            this.entity = spawnLoc.getWorld().spawn(spawnLoc, ItemDisplay.class, display -> {
                ItemStack wardItem = new ItemStack(Material.TORCH);
                ItemMeta meta = wardItem.getItemMeta();
                if (meta != null) {
                    meta.setCustomModelData(1);
                    wardItem.setItemMeta(meta);
                }

                display.setItemStack(wardItem);

                display.setBillboard(org.bukkit.entity.Display.Billboard.FIXED);

                display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);

                display.setMetadata("no_vision_check", new org.bukkit.metadata.FixedMetadataValue(DotaCraft.getInstance(), true));

                // Scale
                // org.joml.Vector3f scale = new org.joml.Vector3f(1.2f, 1.2f, 1.2f);
                // display.setTransformation(new org.bukkit.util.Transformation(
                //     new org.joml.Vector3f(), new org.joml.Quaternionf(), scale, new org.joml.Quaternionf()
                // ));
            });

            location.getWorld().playSound(location, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.2f);
        }

        public void remove() {
            if (!active) return;
            this.active = false;

            if (entity != null && entity.isValid()) {
                entity.remove();
            }
        }

        public UUID getWardId() { return wardId; }
        public Location getLocation() { return location; }
        public double getVisionRadius() { return visionRadius; }
        public String getTeam() { return team; }
        public boolean isActive() { return active; }
        public boolean isExpired() { return System.currentTimeMillis() >= expireTimestamp; }
    }

    private final List<Ward> activeWards = new ArrayList<>();

    public WardManager() {
        startWardCleanupTask();
    }

    public Ward placeObserverWard(Hero hero, Location location) {
        double visionRadius = DotaUnits.toBlocks(1600.0);
        int durationSeconds = 360;

        String team = "radiant"; // temp

        Ward ward = new Ward(location, visionRadius, durationSeconds, team);
        ward.spawn();

        activeWards.add(ward);
        return ward;
    }

    public void unregisterWard(ObserverWard ward) {
        activeWards.remove(ward);
    }

    private void startWardCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Ward> iterator = activeWards.iterator();
                while (iterator.hasNext()) {
                    Ward ward = iterator.next();
                    if (ward.isExpired() || !ward.isActive()) {
                        ward.remove();
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 20L, 20L);
    }

    public List<Ward> getActiveWards() {
        return Collections.unmodifiableList(activeWards);
    }

    public void clearAll() {
        for (Ward ward : activeWards) {
            ward.remove();
        }
        activeWards.clear();
    }
}