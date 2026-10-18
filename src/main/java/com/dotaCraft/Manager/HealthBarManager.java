package com.dotaCraft.Manager;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Objective.Objective;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HealthBarManager {

    private static final Key DOTA_FONT = Key.key("dotacraft", "default");

    // From default.json
    private static final char GREEN_HEALTH_BAR_BASE = '\uE100'; // \uE100 .. \uE132
    private static final char RED_HEALTH_BAR_BASE   = '\uE133'; // \uE133 .. \uE165
    private static final int HEALTH_STATES_COUNT    = 50;       // 51 variant (0..50)

    private final Map<UUID, TextDisplay> activeBars = new ConcurrentHashMap<>();

    /**
     * @param target
     * @param spawnLoc
     * @param name
     * @param currentHealth
     * @param maxHealth
     * @param scaleMultiplier
     * @param viewer
     * @param targetTeam
     */
    public TextDisplay updateOrCreateBar(Entity target, Location spawnLoc, String name, double currentHealth, double maxHealth, float scaleMultiplier, Player viewer, String targetTeam) {
        UUID key = (target != null) ? target.getUniqueId() : (spawnLoc.hashCode() == 0 ? UUID.randomUUID() : UUID.nameUUIDFromBytes(spawnLoc.toString().getBytes()));

        TextDisplay display = activeBars.get(key);

        if (display == null || !display.isValid()) {
            display = spawnLoc.getWorld().spawn(spawnLoc, TextDisplay.class, text -> {
                text.setBillboard(Display.Billboard.CENTER);
                text.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                text.setSeeThrough(false);
                text.setShadowed(false);

                float yOffset = (target != null) ? (float) (target.getHeight()) : 0.0f;

                org.joml.Vector3f translation = new org.joml.Vector3f(0.0f, yOffset, 0.0f);
                org.joml.Vector3f scale = new org.joml.Vector3f(scaleMultiplier, scaleMultiplier, scaleMultiplier);

                org.bukkit.util.Transformation currentTransform = text.getTransformation();
                text.setTransformation(new org.bukkit.util.Transformation(
                        translation,
                        currentTransform.getLeftRotation(),
                        scale,
                        currentTransform.getRightRotation()
                ));
            });

            if (target != null) {
                target.addPassenger(display);
            }

            activeBars.put(key, display);
        }

        boolean isAlly = isAlly(viewer, targetTeam);

        display.text(buildHealthComponent(currentHealth, maxHealth, isAlly));

        return display;
    }

    public TextDisplay updateOrCreateBar(Entity target, Location spawnLoc, String name, double currentHealth, double maxHealth, float scaleMultiplier) {
        return updateOrCreateBar(target, spawnLoc, name, currentHealth, maxHealth, scaleMultiplier, null, "neutral");
    }


    private boolean isAlly(Player viewer, String targetTeam) {
        if (viewer == null || targetTeam == null) return false;
        if (targetTeam.equalsIgnoreCase("neutral")) return false;

        Hero viewerHero = HeroManager.getHero(viewer);
        if (viewerHero == null) return false;

        return viewerHero.getTeam().equalsIgnoreCase(targetTeam);
    }

    private Component buildHealthComponent(double currentHp, double maxHp, boolean isAlly) {
        double ratio = Math.max(0.0, Math.min(1.0, currentHp / maxHp));

        int state = (int) Math.round(ratio * HEALTH_STATES_COUNT);

        char baseChar = isAlly ? GREEN_HEALTH_BAR_BASE : RED_HEALTH_BAR_BASE;
        char barSymbol = (char) (baseChar + state);

        return Component.text(barSymbol).font(DOTA_FONT);
    }

    public void removeBar(Entity target) {
        if (target == null) return;
        removeBar(target.getUniqueId());
    }

    public void removeBar(UUID key) {
        TextDisplay display = activeBars.remove(key);
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    public void clearAll() {
        for (TextDisplay display : activeBars.values()) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        activeBars.clear();
    }
}