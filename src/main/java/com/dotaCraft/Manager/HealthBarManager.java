package com.dotaCraft.Manager;

import com.dotaCraft.DotaCraft;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HealthBarManager {

    private static final Key DOTA_FONT = Key.key("dotacraft", "default");

    private static final char HEALTH_BAR_BASE_CHAR = '\uE100';
    private static final int HEALTH_STATES_COUNT = 50;

    private final Map<UUID, TextDisplay> activeBars = new ConcurrentHashMap<>();

    /**
     *
     * @param target
     * @param spawnLoc
     * @param name
     * @param currentHealth
     * @param maxHealth
     * @param scaleMultiplier
     */
    public TextDisplay updateOrCreateBar(Entity target, Location spawnLoc, String name, double currentHealth, double maxHealth, float scaleMultiplier) {
        UUID key = (target != null) ? target.getUniqueId() : spawnLoc.hashCode() == 0 ? UUID.randomUUID() : UUID.nameUUIDFromBytes(spawnLoc.toString().getBytes());

        TextDisplay display = activeBars.get(key);

        if (display == null || !display.isValid()) {
            display = spawnLoc.getWorld().spawn(spawnLoc, TextDisplay.class, text -> {
                text.setBillboard(Display.Billboard.CENTER);
                text.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                text.setSeeThrough(false);
                text.setShadowed(false);

                if (scaleMultiplier != 1.0f) {
                    org.bukkit.util.Transformation currentTransform = text.getTransformation();

                    text.setTransformation(new org.bukkit.util.Transformation(
                            currentTransform.getTranslation(),
                            currentTransform.getLeftRotation(),
                            new org.joml.Vector3f(scaleMultiplier, scaleMultiplier, scaleMultiplier),
                            currentTransform.getRightRotation()
                    ));
                }

                text.getPersistentDataContainer();
            });

            if (target != null) {
                target.addPassenger(display);
            }

            activeBars.put(key, display);
        }

        display.text(buildHealthComponent(name, currentHealth, maxHealth));

        return display;
    }

    private Component buildHealthComponent(String name, double currentHp, double maxHp) {
        double ratio = Math.max(0.0, Math.min(1.0, currentHp / maxHp));

        int state = (int) Math.round(ratio * HEALTH_STATES_COUNT);
        char barSymbol = (char) (HEALTH_BAR_BASE_CHAR + state);

        NamedTextColor hpColor = ratio > 0.5 ? NamedTextColor.GREEN : (ratio > 0.2 ? NamedTextColor.YELLOW : NamedTextColor.RED);

        return Component.empty()
                .append(Component.text(barSymbol).font(DOTA_FONT));
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