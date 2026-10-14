package com.dotaCraft.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.key.Key;

public class HUDUtils {

    private static final Key DOTA_FONT = Key.key("dotacraft", "default");

    public static Component getSpaceComponent(int pixels) {
        if (pixels == 0) return Component.empty();

        StringBuilder builder = new StringBuilder();

        if (pixels < 0) {
            int abs = -pixels;
            if ((abs & 128) != 0) builder.append("\uF880"); // -128
            if ((abs & 64)  != 0) builder.append("\uF840"); // -64
            if ((abs & 32)  != 0) builder.append("\uF820"); // -32
            if ((abs & 16)  != 0) builder.append("\uF810"); // -16
            if ((abs & 8)   != 0) builder.append("\uF808"); // -8
            if ((abs & 4)   != 0) builder.append("\uF804"); // -4
            if ((abs & 2)   != 0) builder.append("\uF802"); // -2
            if ((abs & 1)   != 0) builder.append("\uF801"); // -1

            int remaining = abs - (abs & 255);
            while (remaining >= 128) {
                builder.append("\uF880");
                remaining -= 128;
            }
        } else {
            int val = pixels;
            if ((val & 128) != 0) builder.append("\uF780"); // +128
            if ((val & 64)  != 0) builder.append("\uF740"); // +64
            if ((val & 32)  != 0) builder.append("\uF720"); // +32
            if ((val & 16)  != 0) builder.append("\uF710"); // +16
            if ((val & 8)   != 0) builder.append("\uF708"); // +8
            if ((val & 4)   != 0) builder.append("\uF704"); // +4
            if ((val & 2)   != 0) builder.append("\uF702"); // +2
            if ((val & 1)   != 0) builder.append("\uF701"); // +1

            int remaining = val - (val & 255);
            while (remaining >= 128) {
                builder.append("\uF780");
                remaining -= 128;
            }
        }

        return Component.text(builder.toString()).font(DOTA_FONT);
    }
}