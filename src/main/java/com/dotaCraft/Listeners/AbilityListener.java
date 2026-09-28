package com.dotaCraft.Listeners;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Hero.impl.pudge.Pudge;
import com.dotaCraft.Item.impl.IronBranch;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.Material;
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
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            handleRightClick(player, event.getItem());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        handleRightClick(player, item);
    }

    private void handleRightClick(Player player, ItemStack item) {

        if (item.getType() == Material.COPPER_SPEAR ) {
            if (!heroManager.hasHero(player)) {
                Pudge pudge = new Pudge(player);
                heroManager.registerHero(player, pudge);
                player.sendMessage("§aYou have picked Pudge!");
                player.sendMessage("§7--- §eStats §7---");
                player.sendMessage("§cStrength: §f" + pudge.getStrength());
                player.sendMessage("§aAgility: §f" + pudge.getAgility());
                player.sendMessage("§bIntelligence: §f" + pudge.getIntellect());
                return;
            }
        } else if (item.getType() == Material.STICK) {
            Hero hero = heroManager.getHero(player);
            if (hero != null) {
                IronBranch branch = new IronBranch();
                hero.addItem(2, branch);
                player.sendMessage("§aYou added Iron Branch to slot 1!");
                player.sendMessage("§7--- §eStats §7---");
                player.sendMessage("§cStrength: §f" + hero.getStrength());
                player.sendMessage("§aAgility: §f" + hero.getAgility());
                player.sendMessage("§bIntelligence: §f" + hero.getIntellect());
            } else {
                player.sendMessage("§cYou need to pick a hero first!");
            }
            return;
        }

        Hero hero = heroManager.getHero(player);
        if (hero != null) {
            hero.castAbility(1);
        }
    }
}