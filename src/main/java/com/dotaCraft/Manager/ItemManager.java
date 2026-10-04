package com.dotaCraft.Manager;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ItemManager {

    private static final Map<String, Supplier<Item>> itemRegistry = new HashMap<>();

    public static void registerItem(String id, Supplier<Item> supplier) {
        itemRegistry.put(id.toLowerCase(), supplier);
    }

    public static Item createItem(String id) {
        Supplier<Item> supplier = itemRegistry.get(id.toLowerCase());
        return supplier != null ? supplier.get() : null;
    }

    public static boolean tryCraftItems(Hero hero) {
        for (Map.Entry<String, Supplier<Item>> entry : itemRegistry.entrySet()) {
            Item sampleItem = entry.getValue().get();

            if (!sampleItem.isRecipeItem()) {
                continue;
            }

            List<String> requiredComponents =
                    new ArrayList<>(sampleItem.getRecipeComponents());

            Map<Integer, Item> foundSlots = new HashMap<>();

            for (int slot = 0; slot < 6; slot++) {
                Item itemInSlot = hero.getItemInSlot(slot);

                if (itemInSlot == null) {
                    continue;
                }

                String itemId = itemInSlot.getId().toLowerCase();

                if (requiredComponents.contains(itemId)) {
                    requiredComponents.remove(itemId);
                    foundSlots.put(slot, itemInSlot);
                }

                if (requiredComponents.isEmpty()) {
                    assembleRecipe(hero, foundSlots, entry.getKey());
                    return true;
                }
            }
        }

        return false;
    }

    private static void assembleRecipe(
            Hero hero,
            Map<Integer, Item> usedSlots,
            String resultItemId
    ) {
        int firstSlot = -1;

        for (int slot : usedSlots.keySet()) {
            if (firstSlot == -1) {
                firstSlot = slot;
            }

            hero.removeItemFromSlot(slot);
        }

        Item resultItem = createItem(resultItemId);

        if (resultItem != null && firstSlot != -1) {
            hero.addItem(firstSlot, resultItem);

            if (hero.getPlayer() != null) {
                hero.getPlayer().playSound(
                        hero.getPlayer().getLocation(),
                        Sound.BLOCK_ANVIL_USE,
                        1.0f,
                        1.2f
                );

                hero.getPlayer().sendMessage(
                        "§a[Shop] §fВы собрали предмет: §e"
                                + resultItem.getId()
                                + "§f!"
                );
            }
        }
    }
}
