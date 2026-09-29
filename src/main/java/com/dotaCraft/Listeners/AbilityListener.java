package com.dotaCraft.Listeners;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Hero.impl.invoker.Invoker;
import com.dotaCraft.Hero.impl.pudge.Pudge;
import com.dotaCraft.Item.impl.ScytheOfVyse;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class AbilityListener implements Listener {

    private final HeroManager heroManager;

    public AbilityListener(HeroManager heroManager) {
        this.heroManager = heroManager;
    }

    @EventHandler
    public void onLeashBreak(EntityUnleashEvent event) {
        if (event.getEntity() instanceof Slime) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Hero hero = heroManager.getHero(player);
        if (hero == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.STICK) {
            Entity target = event.getRightClicked();
            player.sendMessage("§eDebug: Clicked on " + target.getType().name());

            hero.useItem(2, target);

            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item == null) return;

            handleRightClick(player, item);
        }
    }

    private void handleRightClick(Player player, ItemStack item) {
        if (item.getType() == Material.COPPER_SPEAR) {
            if (!heroManager.hasHero(player)) {
                Pudge pudge = new Pudge(player);
                heroManager.registerHero(player, pudge);
                player.sendMessage("§aYou have picked Pudge!");
                printHeroStats(player, pudge);
            }
            return;
        }

        if (item.getType() == Material.BLAZE_ROD) {
            if (!heroManager.hasHero(player)) {
                Invoker invoker = new Invoker(player);
                heroManager.registerHero(player, invoker);
                player.sendMessage("§aYou have picked Invoker!");
                printHeroStats(player, invoker);
            }
            return;
        }

        Hero hero = heroManager.getHero(player);
        if (hero == null) {
            player.sendMessage("§cYou need to pick a hero first!");
            return;
        }

        if (item.getType() == Material.AMETHYST_SHARD || item.getType() == Material.FEATHER) {
            ScytheOfVyse scytheOfVyse = new ScytheOfVyse();
            hero.addItem(2, scytheOfVyse);
            player.sendMessage("§aYou added Scythe of Vyse to Slot 2!");
            printHeroStats(player, hero);
            return;
        }

        if (item.getType() == Material.STICK) {
            hero.useItem(2, null);
            return;
        }

        hero.castAbility(1);
    }

    private void printHeroStats(Player player, Hero hero) {
        player.sendMessage("§7--- §eStats §7---");
        player.sendMessage("§cStrength: §f" + hero.getStrength());
        player.sendMessage("§aAgility: §f" + hero.getAgility());
        player.sendMessage("§bIntelligence: §f" + hero.getIntellect());
    }
}