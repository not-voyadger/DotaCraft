package com.dotaCraft.Objective.Ward;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class ObserverWard {

    private final Location location;
    private final double visionRadius;
    private final long expireTimestamp;
    private final String team;
    private ArmorStand entity;
    private boolean active = true;

    public ObserverWard(Location location, String team) {
        this.location = location;
        this.visionRadius = DotaUnits.toBlocks(1600.0);
        int durationSeconds = 360;
        this.expireTimestamp = System.currentTimeMillis() + (durationSeconds * 1000L);
        this.team = team;
    }

    public ObserverWard(Location location) {
        this(location, "radiant");
    }

    public void spawn() {
        if (location.getWorld() == null) return;

        this.entity = location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setCustomName("§bObserver Ward");
            stand.setCustomNameVisible(true);

            stand.setMetadata("no_vision_check", new FixedMetadataValue(DotaCraft.getInstance(), true));

            if (stand.getEquipment() != null) {
                stand.getEquipment().setHelmet(new ItemStack(Material.ENDER_EYE));
            }
        });

        location.getWorld().playSound(location, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.2f);

        long delayTicks = (expireTimestamp - System.currentTimeMillis()) / 50L;
        new BukkitRunnable() {
            @Override
            public void run() {
                remove();
            }
        }.runTaskLater(DotaCraft.getInstance(), Math.max(1L, delayTicks));
    }

    public void remove() {
        if (!active) return;
        this.active = false;

        if (entity != null && entity.isValid()) {
            entity.remove();
        }

        DotaCraft.getInstance().getWardManager().unregisterWard(this);
    }

    public Location getLocation() { return location; }
    public double getVisionRadius() { return visionRadius; }
    public String getTeam() { return team; }
    public boolean isActive() { return active; }
    public boolean isExpired() { return System.currentTimeMillis() >= expireTimestamp; }
}