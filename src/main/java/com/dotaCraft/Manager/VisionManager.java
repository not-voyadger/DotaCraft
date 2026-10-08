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
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class VisionManager implements Listener {

    private static class VisionSource {
        private final Location location;
        private final double visionRadius;

        public VisionSource(Location location, double visionRadius) {
            this.location = location;
            this.visionRadius = visionRadius;
        }

        public Location getLocation() { return location; }
        public double getVisionRadius() { return visionRadius; }
    }

    private final Map<UUID, Set<UUID>> hiddenEntities = new ConcurrentHashMap<>();
    private final Map<UUID, Set<Location>> playerLightBlocks = new ConcurrentHashMap<>();

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

                    processPlayerVision(viewer, viewerHero);
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 3L);
    }

    private void processPlayerVision(Player viewer, Hero viewerHero) {
        Location currentLoc = viewer.getLocation();
        long worldTime = viewer.getWorld().getTime();
        boolean isDay = worldTime < 12300 || worldTime > 23850;

        Location eyeLoc = viewer.getEyeLocation();
        World world = viewer.getWorld();

        List<VisionSource> visionSources = new ArrayList<>();

        visionSources.add(new VisionSource(eyeLoc, viewerHero.getCurrentVisionRadius()));

        for (WardManager.Ward ward : DotaCraft.getInstance().getWardManager().getActiveWards()) {
            if (ward.getTeam() != null && ward.getTeam().equalsIgnoreCase(viewerHero.getTeam())) {
                visionSources.add(new VisionSource(ward.getLocation().clone().add(0, 1.8, 0), ward.getVisionRadius()));
            }
        }

        if (viewer.hasPotionEffect(PotionEffectType.DARKNESS)) {
            viewer.removePotionEffect(PotionEffectType.DARKNESS);
        }
        if (viewer.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            viewer.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }

        long clientTime = isDay ? 12800L : 18000L;
        viewer.setPlayerTime(clientTime, false);

        int lightLevel = isDay ? 15 : 10;
        Light lightData = (Light) Material.LIGHT.createBlockData();
        lightData.setLevel(lightLevel);

        Set<Location> activeLights = playerLightBlocks.computeIfAbsent(viewer.getUniqueId(), k -> new HashSet<>());
        Set<Location> newLights = new HashSet<>();

        for (VisionSource source : visionSources) {
            newLights.addAll(calculateDynamicLightGrid(source.getLocation(), source.getLocation(), source.getVisionRadius()));
        }

        for (Location oldLoc : activeLights) {
            if (!newLights.contains(oldLoc)) {
                viewer.sendBlockChange(oldLoc, oldLoc.getBlock().getBlockData());
            }
        }

        for (Location newLoc : newLights) {
            if (!activeLights.contains(newLoc)) {
                viewer.sendBlockChange(newLoc, lightData);
            }
        }
        playerLightBlocks.put(viewer.getUniqueId(), newLights);

        Set<UUID> currentlyHidden = hiddenEntities.computeIfAbsent(viewer.getUniqueId(), k -> new HashSet<>());
        Set<UUID> toShow = new HashSet<>();
        Set<UUID> toHide = new HashSet<>();

        double checkRadius = viewerHero.getCurrentVisionRadius() + 25.0;

        for (Entity target : world.getNearbyEntities(eyeLoc, checkRadius, checkRadius, checkRadius)) {
            if (!(target instanceof LivingEntity livingTarget) || target.equals(viewer)) {
                continue;
            }

            if (target instanceof ArmorStand || target instanceof ItemDisplay || target.hasMetadata("no_vision_check")) {
                continue;
            }

            UUID targetId = target.getUniqueId();
            boolean canSee = false;

            if (isAlly(viewer, target)) {
                canSee = true;
            } else {
                Location targetEyeLoc = livingTarget.getEyeLocation();

                for (VisionSource source : visionSources) {
                    Location sourceLoc = source.getLocation();
                    double radiusSq = source.getVisionRadius() * source.getVisionRadius();

                    if (sourceLoc.distanceSquared(targetEyeLoc) <= radiusSq && hasLineOfSight(sourceLoc, targetEyeLoc)) {
                        canSee = true;
                        break;
                    }
                }
            }

            if (canSee) {
                if (currentlyHidden.contains(targetId)) {
                    toShow.add(targetId);
                }
            } else {
                if (!currentlyHidden.contains(targetId)) {
                    toHide.add(targetId);
                }
            }
        }

        for (UUID id : toShow) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) {
                viewer.showEntity(DotaCraft.getInstance(), e);
                currentlyHidden.remove(id);

                if (e instanceof LivingEntity living) {
                    living.setSilent(false);
                }
            }
        }

        for (UUID id : toHide) {
            Entity e = Bukkit.getEntity(id);
            if (e != null) {
                viewer.hideEntity(DotaCraft.getInstance(), e);
                currentlyHidden.add(id);

                if (e instanceof LivingEntity living) {
                    living.setSilent(true);
                }
            }
        }
    }

    private Set<Location> calculateDynamicLightGrid(Location eyeLoc, Location center, double radius) {
        Set<Location> newLights = new HashSet<>();
        int step = 3;
        int intRadius = (int) radius;

        Location groundCenter = center.clone();

        for (int x = -intRadius; x <= intRadius; x += step) {
            for (int z = -intRadius; z <= intRadius; z += step) {
                if ((x * x + z * z) <= (radius * radius)) {
                    Location targetLoc = groundCenter.clone().add(x, 0, z);

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
        return newLights;
    }

    private Block getAirBlockAboveGround(Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        Block baseBlock = loc.getBlock();

        for (int yOffset = -5; yOffset <= 5; yOffset++) {
            Block current = baseBlock.getRelative(0, yOffset, 0);
            Block above = current.getRelative(0, 1, 0);

            if (!current.getType().isAir() && current.getType().isSolid() && above.getType().isAir()) {
                return above;
            }
        }

        if (baseBlock.getType().isAir() && !baseBlock.getRelative(0, -1, 0).getType().isAir()) {
            return baseBlock;
        }

        return null;
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
        return false;
    }

    public void clearPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        hiddenEntities.remove(uuid);

        Set<Location> lights = playerLightBlocks.remove(uuid);
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