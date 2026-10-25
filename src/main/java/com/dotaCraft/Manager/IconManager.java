package com.dotaCraft.Manager;

import com.dotaCraft.Utils.HUDUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IconManager {

    private static final Key DOTA_FONT = Key.key("dotacraft", "default");
    private static final char INDICATOR_BASE_CHAR = '\uE200'; // \uE200 .. \uE21E
    private static final int TOTAL_FRAMES = 31;
    private static final int ICON_WIDTH_PX = 38;

    private static final Map<UUID, List<ActiveBuff>> activeBuffs = new ConcurrentHashMap<>();

    public static class ActiveBuff {
        private final String iconSymbol;
        private final double totalDurationTicks;
        private double remainingTicks;

        public ActiveBuff(String iconSymbol, double totalDurationTicks) {
            this.iconSymbol = iconSymbol;
            this.totalDurationTicks = totalDurationTicks;
            this.remainingTicks = totalDurationTicks;
        }

        public boolean tick() {
            this.remainingTicks -= 1.0;
            return this.remainingTicks <= 0;
        }

        public String getIconSymbol() { return iconSymbol; }
        public double getTotalDurationTicks() { return totalDurationTicks; }
        public double getRemainingTicks() { return remainingTicks; }
    }

    public static void spawnPositiveIcon(Player player, String iconFont, double durationInTicks) {
        if (player == null || durationInTicks <= 0) return;

        List<ActiveBuff> buffs = activeBuffs.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());

        boolean updated = false;
        for (ActiveBuff buff : buffs) {
            if (buff.getIconSymbol().equals(iconFont)) {
                buffs.remove(buff);
                buffs.add(new ActiveBuff(iconFont, durationInTicks));
                updated = true;
                break;
            }
        }

        if (!updated) {
            buffs.add(new ActiveBuff(iconFont, durationInTicks));
        }
    }

    public static void onTick(Player player) {
        List<ActiveBuff> buffs = activeBuffs.get(player.getUniqueId());
        if (buffs == null || buffs.isEmpty()) return;

        Iterator<ActiveBuff> iterator = buffs.iterator();
        while (iterator.hasNext()) {
            ActiveBuff buff = iterator.next();
            if (buff.tick()) {
                iterator.remove();
            }
        }
    }

    public static Component renderBuffsComponent(Player player) {
        List<ActiveBuff> buffs = activeBuffs.get(player.getUniqueId());
        if (buffs == null || buffs.isEmpty()) {
            return Component.empty();
        }

        Component result = Component.empty();

        for (ActiveBuff buff : buffs) {
            Component buffComponent = buildEffectWithIcon(
                    buff.getIconSymbol(),
                    buff.getRemainingTicks(),
                    buff.getTotalDurationTicks()
            );

            result = result.append(buffComponent).append(HUDUtils.getSpaceComponent(2));
        }

        return result;
    }

    private static Component buildEffectWithIcon(String iconSymbol, double remainingTicks, double totalTicks) {
        if (totalTicks <= 0) return Component.empty();

        double ratio = Math.max(0.0, Math.min(1.0, (totalTicks - remainingTicks) / totalTicks));
        int frameIndex = (int) Math.round(ratio * (TOTAL_FRAMES - 1));
        char indicatorSymbol = (char) (INDICATOR_BASE_CHAR + frameIndex);

        int xOffset = 8;

        Component iconComponent = Component.text(iconSymbol)
                .font(DOTA_FONT)
                .shadowColor(ShadowColor.none());

        Component indicatorComponent = Component.text(indicatorSymbol)
                .font(DOTA_FONT)
                .shadowColor(ShadowColor.none());

        return Component.empty()
                .append(indicatorComponent)
                .append(HUDUtils.getSpaceComponent(-ICON_WIDTH_PX + xOffset))
                .append(iconComponent);
    }

    public static void clearPlayer(Player player) {
        activeBuffs.remove(player.getUniqueId());
    }
}