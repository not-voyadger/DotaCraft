package com.dotaCraft.Listeners;

import com.dotaCraft.Bind.BindTrigger;
import com.dotaCraft.Bind.PlayerBinds;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityListener implements Listener {

    private final HeroManager heroManager;
    private final Map playerBindsMap = new HashMap<>();

    public AbilityListener(HeroManager heroManager) {
        this.heroManager = heroManager;
    }

    private PlayerBinds getBinds(Player player) {
        return (PlayerBinds) playerBindsMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerBinds());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        BindTrigger trigger = switch (event.getNewSlot()) {
            case 0 -> BindTrigger.SLOT_1;
            case 1 -> BindTrigger.SLOT_2;
            case 2 -> BindTrigger.SLOT_3;
            case 3 -> BindTrigger.SLOT_4;
            case 4 -> BindTrigger.SLOT_5;
            case 5 -> BindTrigger.SLOT_6;
            case 6 -> BindTrigger.SLOT_7;
            case 7 -> BindTrigger.SLOT_8;
            case 8 -> BindTrigger.SLOT_9;
            default -> null;
        };

        if (trigger != null) {
            PlayerBinds binds = getBinds(player);
            if (binds.hasTrigger(trigger)) {
                event.setCancelled(true);
                handleTrigger(hero, binds.getBindTarget(trigger));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        event.setCancelled(true);
        PlayerBinds binds = getBinds(player);
        handleTrigger(hero, binds.getBindTarget(BindTrigger.PRESS_Q));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        event.setCancelled(true);
        PlayerBinds binds = getBinds(player);
        handleTrigger(hero, binds.getBindTarget(BindTrigger.PRESS_F));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        PlayerBinds binds = getBinds(player);
        handleTrigger(hero, binds.getBindTarget(BindTrigger.PRESS_SHIFT));
    }

    private void handleTrigger(Hero hero, PlayerBinds.BindTarget bindTarget) {
        if (bindTarget == null) return;

        if (bindTarget.getType() == BindTrigger.BindType.ABILITY) {
            hero.castAbility(bindTarget.getTargetSlot());
        } else if (bindTarget.getType() == BindTrigger.BindType.ITEM) {
            hero.useItem(bindTarget.getTargetSlot());
        }
    }
}