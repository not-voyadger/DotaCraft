package com.dotaCraft.Manager;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VisionManager implements Listener {

    private final Map<UUID, Set<UUID>> hiddenEntities = new HashMap<>();
    private final Map<UUID, Set<Location>> playerLightBlocks = new HashMap<>();

    public VisionManager() {
        startVisionTask();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearPlayer(event.getPlayer());
    }

    private void startVisionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player viewer : Bukkit.getOnlinePlayers()) {
                    Hero viewerHero = HeroManager.getHero(viewer);
                    if (viewerHero == null || !viewer.isOnline() || viewer.isDead()) continue;

                    if (viewer.hasPotionEffect(PotionEffectType.DARKNESS)) {
                        viewer.removePotionEffect(PotionEffectType.DARKNESS);
                    }
                    if (viewer.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
                        viewer.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    }

                    updateVisionForPlayer(viewer, viewerHero);
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 2L);
    }

    private void updateVisionForPlayer(Player viewer, Hero viewerHero) {
        long worldTime = viewer.getWorld().getTime();
        boolean isDay = worldTime < 12300 || worldTime > 23850;

        long clientTime = isDay ? 12800L : 18000L;
        viewer.setPlayerTime(clientTime, false);

        Location eyeLoc = viewer.getEyeLocation();
        World world = viewer.getWorld();

        double visionRadius = viewerHero.getCurrentVisionRadius();
        double visionRadiusSq = visionRadius * visionRadius;

        int lightLevel = isDay ? 15 : 9;
        updateDynamicLightGrid(viewer, eyeLoc, visionRadius, lightLevel);

        Set<UUID> currentlyHidden = hiddenEntities.computeIfAbsent(viewer.getUniqueId(), k -> new HashSet<>());
        double checkRadius = visionRadius + 10.0;

        for (Entity target : world.getNearbyEntities(eyeLoc, checkRadius, checkRadius, checkRadius)) {
            if (!(target instanceof LivingEntity livingTarget) || target.equals(viewer)) {
                continue;
            }

            if (target instanceof ArmorStand || target.hasMetadata("no_vision_check")) {
                continue;
            }

            UUID targetId = target.getUniqueId();
            boolean canSee = false;

            if (isAlly(viewer, target)) {
                canSee = true;
            } else {
                Location targetEyeLoc = livingTarget.getEyeLocation();
                double distSq = eyeLoc.distanceSquared(targetEyeLoc);

                if (distSq <= visionRadiusSq) {
                    if (hasLineOfSight(eyeLoc, targetEyeLoc)) {
                        canSee = true;
                    }
                }
            }

            if (canSee) {
                if (currentlyHidden.contains(targetId)) {
                    viewer.showEntity(DotaCraft.getInstance(), target);
                    currentlyHidden.remove(targetId);
                }
            } else {
                if (!currentlyHidden.contains(targetId)) {
                    viewer.hideEntity(DotaCraft.getInstance(), target);
                    currentlyHidden.add(targetId);
                }
            }
        }
    }

    private void updateDynamicLightGrid(Player player, Location eyeLoc, double radius, int lightLevel) {
        Set<Location> activeLights = playerLightBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        Set<Location> newLights = new HashSet<>();

        Light lightData = (Light) Material.LIGHT.createBlockData();
        lightData.setLevel(lightLevel);

        Location center = player.getLocation();
        int step = 4;
        int intRadius = (int) radius;

        for (int x = -intRadius; x <= intRadius; x += step) {
            for (int z = -intRadius; z <= intRadius; z += step) {
                if ((x * x + z * z) <= (radius * radius)) {
                    Location targetLoc = center.clone().add(x, 0, z);

                    Block surfaceBlock = getAirBlockAboveGround(targetLoc);
                    if (surfaceBlock != null) {
                        Location lightBlockLoc = surfaceBlock.getLocation().add(0.5, 0.5, 0.5);

                        if (hasLineOfSight(eyeLoc, lightBlockLoc)) {
                            newLights.add(surfaceBlock.getLocation());
                        }
                    }
                }
            }
        }

        for (Location oldLoc : activeLights) {
            if (!newLights.contains(oldLoc)) {
                player.sendBlockChange(oldLoc, oldLoc.getBlock().getBlockData());
            }
        }

        for (Location newLoc : newLights) {
            if (!activeLights.contains(newLoc)) {
                player.sendBlockChange(newLoc, lightData);
            }
        }

        playerLightBlocks.put(player.getUniqueId(), newLights);
    }

    private Block getAirBlockAboveGround(Location loc) {
        Block block = loc.getBlock();

        for (int i = 0; i < 6; i++) {
            if (block.getType().isAir() && !block.getRelative(0, -1, 0).getType().isAir()) {
                return block;
            }
            block = (i % 2 == 0) ? block.getRelative(0, 1, 0) : block.getRelative(0, -2, 0);
        }
        return loc.getBlock();
    }

    private boolean hasLineOfSight(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = from.distance(to);

        if (distance <= 0.001) return true;

        RayTraceResult rayTrace = from.getWorld().rayTraceBlocks(
                from,
                direction.normalize(),
                distance,
                FluidCollisionMode.NEVER,
                true
        );

        return rayTrace == null || rayTrace.getHitBlock() == null;
    }

    private boolean isAlly(Player viewer, Entity target) {
        // TODO
        return false;
    }

    public void clearPlayer(Player player) {
        hiddenEntities.remove(player.getUniqueId());

        Set<Location> lights = playerLightBlocks.remove(player.getUniqueId());
        if (lights != null) {
            for (Location loc : lights) {
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
            }
        }

        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.resetPlayerTime();
    }
}