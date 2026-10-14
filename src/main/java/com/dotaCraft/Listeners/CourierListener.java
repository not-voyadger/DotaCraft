package com.dotaCraft.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CourierListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().toString().contains("\uE001")) {
            event.setCancelled(true);

            if (event.getClickedInventory() == null || !(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getSlot();

            switch (slot) {
                case 3 -> player.sendMessage("§a[Курьер] Выбран курьер!");
                case 4 -> player.sendMessage("§a[Курьер] Забраны предметы из тайника!");
                case 5 -> player.sendMessage("§a[Курьер] Курьер несёт предметы!");
                case 6 -> player.sendMessage("§c[Курьер] Активировано ускорение!");
            }
        }
    }
}