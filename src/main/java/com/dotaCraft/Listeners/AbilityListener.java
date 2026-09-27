package com.dotaCraft.Listeners;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Hero.impl.pudge.Pudge;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (!heroManager.hasHero(player)) {
                if (event.getItem() != null && event.getItem().getType() == Material.COPPER_SPEAR) {
                    heroManager.registerHero(player, new Pudge(player));
                    player.sendMessage("§aYou have picked Pudge!");
                }
                return;
            }

            Hero hero = heroManager.getHero(player);

            if (event.getItem() != null && event.getItem().getType() == Material.COPPER_SPEAR) {
                hero.castAbility(1);
            }
        }
    }
}