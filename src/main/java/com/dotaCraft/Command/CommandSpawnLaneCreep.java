package com.dotaCraft.Command;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Units.LaneCreep;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;

public class CommandSpawnLaneCreep implements BasicCommand {

    private final DotaCraft plugin;

    public CommandSpawnLaneCreep(DotaCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage("§cCommand for players only.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§eUse: §f/spawnlane <radiant|dire> <melee|ranged|siege> [count]");
            return;
        }

        String team = args[0].toLowerCase();
        if (!team.equals("radiant") && !team.equals("dire")) {
            player.sendMessage("§Choose 'radiant' or 'dire'.");
            return;
        }

        String typeStr = args[1].toUpperCase();
        LaneCreep.CreepType creepType;
        try {
            creepType = LaneCreep.CreepType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cUnknown creep type! Available types: melee, ranged, siege.");
            return;
        }

        int count = 1;
        if (args.length >= 3) {
            try {
                count = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException ignored) {
                player.sendMessage("§cIncorrect count, 1 spawned.");
            }
        }

        Location spawnLoc = player.getLocation();

        for (int i = 0; i < count; i++) {
            LaneCreep creep = new LaneCreep(creepType, team, spawnLoc, Collections.emptyList());
            creep.spawn(spawnLoc);

            plugin.getCreepLaneManager().getActiveLaneCreeps().add(creep);
        }

        String teamColor = team.equals("radiant") ? "§a" : "§c";
        player.sendMessage("§a[LaneCreep] Spawned " + count + " " + teamColor + team.toUpperCase() + " §f" + creepType.name() + " crips.");
    }
}