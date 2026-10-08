package com.dotaCraft.Manager.sources;

import org.bukkit.Location;

public interface VisionSource {
    Location getLocation();
    double getVisionRadius();
    String getTeam();
    boolean isValid();
}