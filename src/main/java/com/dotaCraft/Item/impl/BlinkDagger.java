package com.dotaCraft.Item.impl;

import com.dotaCraft.Item.Item;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlinkDagger extends Item {
    public BlinkDagger() {
        super(
                "blink_dagger",
                TargetTypes.POINT_TARGET,
                2250,
                15.0,
                0,
                1200,
                0,
                false
        );
    }

    @Override
    public void onUsePointTarget(Player player, Location targetLocation) {
        if (player == null || !player.isOnline()) return;

        World world = player.getWorld();
        Location startLoc = player.getLocation();
        double maxRange = getCastRange();

        world.playSound(startLoc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.2f);
        world.spawnParticle(Particle.REVERSE_PORTAL, startLoc.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);

        Location destination;

        double distance = startLoc.distance(targetLocation);

        if (distance <= maxRange && targetLocation.getWorld().equals(world)) {
            destination = targetLocation.clone();
        } else {
            Vector direction = targetLocation.toVector().subtract(startLoc.toVector()).normalize();
            destination = startLoc.clone().add(direction.multiply(maxRange));
        }

        destination.setPitch(startLoc.getPitch());
        destination.setYaw(startLoc.getYaw());

        destination = getSafeBlinkLocation(destination);

        player.teleport(destination);

        world.playSound(destination, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.5f);
        world.spawnParticle(Particle.PORTAL, destination.clone().add(0, 1, 0), 40, 0.3, 0.5, 0.3, 0.2);
    }

    private Location getSafeBlinkLocation(Location loc) {
        Block targetBlock = loc.getBlock();
        World world = loc.getWorld();

        while (targetBlock.getType().isSolid() && targetBlock.getY() < world.getMaxHeight()) {
            targetBlock = targetBlock.getRelative(0, 1, 0);
        }

        int dropLimit = 6;
        while (!targetBlock.getRelative(0, -1, 0).getType().isSolid()
                && targetBlock.getY() > world.getMinHeight()
                && dropLimit > 0) {
            targetBlock = targetBlock.getRelative(0, -1, 0);
            dropLimit--;
        }

        Location safeLoc = targetBlock.getLocation().add(0.5, 0, 0.5);
        safeLoc.setYaw(loc.getYaw());
        safeLoc.setPitch(loc.getPitch());
        return safeLoc;
    }
}