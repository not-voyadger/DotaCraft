package com.dotaCraft.UI;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GoldFrameGUI {

    private static final Key DOTA_FONT = Key.key("dotacraft", "default");
    private static final String GUI_BG_CHAR = "\uE001";
    private static final String OFFSET_TO_LEFT = "\uF80C\uF80C";

    public static void openCourierMenu(Player player) {
        Component titleComponent = Component.text(OFFSET_TO_LEFT).font(DOTA_FONT)
                .append(Component.text(GUI_BG_CHAR).font(DOTA_FONT));

        Inventory gui = Bukkit.createInventory(null, 9, titleComponent);

        gui.setItem(3, createButton(Material.STRUCTURE_VOID, "§aВыбрать курьера", 1001));
        gui.setItem(4, createButton(Material.STRUCTURE_VOID, "§aЗабрать вещи", 1002));
        gui.setItem(5, createButton(Material.STRUCTURE_VOID, "§aДоставить предмет", 1003));
        gui.setItem(6, createButton(Material.STRUCTURE_VOID, "§cУскорение", 1004));

        player.openInventory(gui);
    }

    private static ItemStack createButton(Material material, String name, int customModelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name));
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }
        return item;
    }
}