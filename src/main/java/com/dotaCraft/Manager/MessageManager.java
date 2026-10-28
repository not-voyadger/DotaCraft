package com.dotaCraft.Manager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;

public class MessageManager {

    private static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "DotaCraft" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    public MessageManager() {}

    public static void broadcastKill(Player killer, Player victim, int killerGold, int assistGold, int totalAssistHeroes) {
        StringBuilder message = new StringBuilder();

        //Killer
        message.append(ChatColor.GREEN).append(killer.getName());
        message.append(ChatColor.GRAY).append(" killed ");

        //Victim
        message.append(ChatColor.RED).append(victim.getName()).append(" ");

        // Primary Gold
        message.append(ChatColor.GOLD).append(killerGold).append("g ");

        if (assistGold > 0 && totalAssistHeroes > 0) {
            message.append(ChatColor.YELLOW).append("+ ").append(assistGold).append("g ");
            message.append(ChatColor.GRAY).append("among ").append(totalAssistHeroes).append(" heroes");
        }

        broadcast(message.toString());
    }

    public static void broadcastSoloKill(Player killer, Player victim, int killerGold) {
        broadcastKill(killer, victim, killerGold, 0, 0);
    }

    public static void broadcastObjectiveDestroy(Player player, String objective) {
        // TODO
    }

    public static void broadcastTeamStructureFortify(String team) {
        if (team == null) {
            return;
        }

        if (team == "radiant") {
            Bukkit.broadcastMessage(ChatColor.GREEN + "Radiant structured fortified");
            //sound
        } else {
            Bukkit.broadcastMessage(ChatColor.RED + "Dire structured fortified");
            //sound
        }
    }

    public static void broadcast(String message) {
        Bukkit.broadcastMessage(PREFIX + colorize(message));
    }

    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void sendToTeam(Collection<Player> team, String message) {
        String formatted = PREFIX + colorize(message);
        for (Player player : team) {
            player.sendMessage(formatted);
        }
    }
}
