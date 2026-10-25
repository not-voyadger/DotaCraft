package com.dotaCraft.Listeners;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        Hero hero = HeroManager.getHero(player);

        String heroName;
        ChatColor teamColor;

        if (hero != null) {
            heroName = hero.getHeroName();
            String team = hero.getTeam();

            if (team != null && team.equalsIgnoreCase("radiant")) {
                teamColor = ChatColor.AQUA;
            } else if (team != null && team.equalsIgnoreCase("dire")) {
                teamColor = ChatColor.RED;
            } else {
                teamColor = ChatColor.GOLD;
            }
        } else {
            heroName = "No hero";
            teamColor = ChatColor.GRAY;
        }

        String customFormat = "%1$s " + ChatColor.GRAY + "(" + teamColor + heroName + ChatColor.GRAY + ")" + ChatColor.RESET + ": %2$s";

        event.setFormat(customFormat);
    }
}