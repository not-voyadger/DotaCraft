package com.dotaCraft.Manager;

import com.dotaCraft.Utils.HUDUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    private static final char STACK_DIGIT_BASE_CHAR = '\uE600'; // \uE600 .. \uE609 (цифры 0..9)
    private static final int TOTAL_FRAMES = 31;

    private static final int ICON_WIDTH_PX = 38;
    private static final int ICON_STEP_PX = 18;

    public static final double PERMANENT_DURATION = -1;

    private static final int STACK_NUMBER_OFFSET_X = -4;
    private static final int DIGIT_WIDTH_PX = 5;

    private static final Map<UUID, List<ActiveBuff>> positiveBuffs = new ConcurrentHashMap<>();
    private static final Map<UUID, List<ActiveBuff>> negativeBuffs = new ConcurrentHashMap<>();

    public static class ActiveBuff {
        private final String iconSymbol;
        private double totalDurationTicks;
        private double remainingTicks;
        private final boolean isPermanent;
        private int stackCount;

        public ActiveBuff(String iconSymbol, double totalDurationTicks) {
            this.iconSymbol = iconSymbol;
            this.totalDurationTicks = totalDurationTicks;
            this.remainingTicks = totalDurationTicks;
            this.isPermanent = false;
            this.stackCount = 0;
        }

        public ActiveBuff(String iconSymbol) {
            this(iconSymbol, 0);
        }

        public ActiveBuff(String iconSymbol, int stackCount) {
            this.iconSymbol = iconSymbol;
            this.isPermanent = true;
            this.totalDurationTicks = 1;
            this.remainingTicks = 1;
            this.stackCount = stackCount;
        }

        public boolean tick() {
            if (this.isPermanent) {
                return false;
            }
            this.remainingTicks -= 1.0;
            return this.remainingTicks <= 0;
        }

        public String getIconSymbol() { return iconSymbol; }
        public double getTotalDurationTicks() { return totalDurationTicks; }
        public double getRemainingTicks() { return remainingTicks; }
        public boolean isPermanent() { return isPermanent; }
        public int getStackCount() { return stackCount; }
        public void setStackCount(int stackCount) { this.stackCount = stackCount; }
    }

    public static void spawnPositiveIcon(Player player, String iconFont, double durationInTicks) {
        if (player == null) return;
        if (durationInTicks <= 0 && durationInTicks != PERMANENT_DURATION) return;

        List<ActiveBuff> buffs = positiveBuffs.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        buffs.removeIf(buff -> buff.getIconSymbol().equals(iconFont));
        buffs.add(new ActiveBuff(iconFont, durationInTicks));
    }

    public static void spawnPositiveIcon(Player player, String iconFont) {
        if (player == null) return;

        List<ActiveBuff> buffs = positiveBuffs.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        buffs.add(new ActiveBuff(iconFont));
    }

    public static void spawnPositiveIconWithStack(Player player, String iconFont, int stackCount) {
        if (player == null) return;

        List<ActiveBuff> buffs = positiveBuffs.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());

        for (ActiveBuff buff : buffs) {
            if (buff.getIconSymbol().equals(iconFont)) {
                buff.setStackCount(stackCount);
                return;
            }
        }

        buffs.add(new ActiveBuff(iconFont, stackCount));
    }

    public static void spawnNegativeIcon(Player player, String iconFont, double durationInTicks) {
        if (player == null) return;
        if (durationInTicks <= 0 && durationInTicks != PERMANENT_DURATION) return;

        List<ActiveBuff> buffs = negativeBuffs.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        buffs.removeIf(buff -> buff.getIconSymbol().equals(iconFont));
        buffs.add(new ActiveBuff(iconFont, durationInTicks));
    }

    public static void spawnNegativeIcon(Player player, String iconFont) {
        if (player == null) return;
        List<ActiveBuff> buffs = negativeBuffs.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        buffs.removeIf(buff -> buff.getIconSymbol().equals(iconFont));
        buffs.add(new ActiveBuff(iconFont));
    }

    public static void onTick(Player player) {
        tickList(positiveBuffs.get(player.getUniqueId()));
        tickList(negativeBuffs.get(player.getUniqueId()));
    }

    private static void tickList(List<ActiveBuff> buffs) {
        if (buffs == null || buffs.isEmpty()) return;
        Iterator<ActiveBuff> iterator = buffs.iterator();
        while (iterator.hasNext()) {
            ActiveBuff buff = iterator.next();
            if (buff.tick()) {
                iterator.remove();
            }
        }
    }

    public static Component renderPositiveBuffsComponent(Player player) {
        List<ActiveBuff> buffs = positiveBuffs.get(player.getUniqueId());
        if (buffs == null || buffs.isEmpty()) {
            return Component.empty();
        }

        Component result = Component.empty();
        for (int i = buffs.size() - 1; i >= 0; i--) {
            ActiveBuff buff = buffs.get(i);
            Component buffComponent = buildEffectWithIcon(
                    buff.getIconSymbol(),
                    buff.getRemainingTicks(),
                    buff.getTotalDurationTicks(),
                    buff.isPermanent(),
                    buff.getStackCount()
            );
            result = result.append(buffComponent);
        }
        return result;
    }

    public static Component renderNegativeBuffsComponent(Player player) {
        List<ActiveBuff> buffs = negativeBuffs.get(player.getUniqueId());
        if (buffs == null || buffs.isEmpty()) {
            return Component.empty();
        }

        Component result = Component.empty();
        for (int i = buffs.size() - 1; i >= 0; i--) {
            ActiveBuff buff = buffs.get(i);
            Component buffComponent = buildEffectWithIcon(
                    buff.getIconSymbol(),
                    buff.getRemainingTicks(),
                    buff.getTotalDurationTicks(),
                    buff.isPermanent(),
                    buff.getStackCount()
            );
            result = result.append(buffComponent);
        }
        return result;
    }

    private static Component buildEffectWithIcon(String iconSymbol, double remainingTicks, double totalTicks, boolean isPermanent, int stackCount) {
        Component indicatorComponent;

        if (isPermanent) {
            indicatorComponent = Component.text(INDICATOR_BASE_CHAR)
                    .font(DOTA_FONT)
                    .shadowColor(ShadowColor.none());
        } else {
            if (totalTicks <= 0) return Component.empty();
            double ratio = Math.max(0.0, Math.min(1.0, (totalTicks - remainingTicks) / totalTicks));
            int frameIndex = (int) Math.round(ratio * (TOTAL_FRAMES - 1));
            char indicatorSymbol = (char) (INDICATOR_BASE_CHAR + frameIndex);

            indicatorComponent = Component.text(indicatorSymbol)
                    .font(DOTA_FONT)
                    .shadowColor(ShadowColor.none());
        }

        Component iconComponent = Component.text(iconSymbol)
                .font(DOTA_FONT)
                .shadowColor(ShadowColor.none());

        Component result = Component.empty()
                .append(indicatorComponent)
                .append(HUDUtils.getSpaceComponent(-ICON_WIDTH_PX + 12))
                .append(iconComponent);

        if (stackCount > 0) {
            String rawNumber = String.valueOf(stackCount);
            Component stackComponent = buildStackNumberComponent(rawNumber);

            int textWidthPx = rawNumber.length() * DIGIT_WIDTH_PX - (rawNumber.length() - 1);
            int centeredOffset = STACK_NUMBER_OFFSET_X - (textWidthPx / 2);

            result = result
                    .append(HUDUtils.getSpaceComponent(centeredOffset))
                    .append(stackComponent)
                    .append(HUDUtils.getSpaceComponent(-(centeredOffset + textWidthPx)));
        }

        return result.append(HUDUtils.getSpaceComponent(-ICON_WIDTH_PX + ICON_STEP_PX - 20));
    }

    private static Component buildStackNumberComponent(String numberStr) {
        Component result = Component.empty();

        for (int i = 0; i < numberStr.length(); i++) {
            int digit = numberStr.charAt(i) - '0';
            if (digit >= 0 && digit <= 9) {
                if (i > 0) {
                    result = result.append(HUDUtils.getSpaceComponent(-1));
                }
                char digitChar = (char) (STACK_DIGIT_BASE_CHAR + digit);
                result = result.append(
                        Component.text(digitChar)
                                .font(DOTA_FONT)
                                .color(NamedTextColor.WHITE)
                                .shadowColor(ShadowColor.shadowColor(0, 0, 0, 255))
                );
            }
        }

        return result;
    }

    private static String formatStackDigits(String numberStr) {
        StringBuilder sb = new StringBuilder(numberStr.length());
        for (int i = 0; i < numberStr.length(); i++) {
            int digit = numberStr.charAt(i) - '0';
            if (digit >= 0 && digit <= 9) {
                sb.append((char) (STACK_DIGIT_BASE_CHAR + digit));
            }
        }
        return sb.toString();
    }

    public static void clearPlayer(Player player) {
        positiveBuffs.remove(player.getUniqueId());
        negativeBuffs.remove(player.getUniqueId());
    }

    public static void clearOrbIcons(Player player) {
        List<ActiveBuff> buffs = positiveBuffs.get(player.getUniqueId());
        if (buffs == null) return;
        buffs.removeIf(buff ->
                buff.getIconSymbol().equals("\uE401") ||
                        buff.getIconSymbol().equals("\uE402") ||
                        buff.getIconSymbol().equals("\uE403")
        );
    }

    public static void removePositiveIcon(Player player, String... iconSymbols) {
        if (player == null || iconSymbols == null) return;
        List<ActiveBuff> buffs = positiveBuffs.get(player.getUniqueId());
        if (buffs == null) return;

        for (String symbol : iconSymbols) {
            buffs.removeIf(buff -> buff.getIconSymbol().equals(symbol));
        }
    }
}