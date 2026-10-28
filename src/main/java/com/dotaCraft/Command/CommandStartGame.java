package com.dotaCraft.Command;

import com.dotaCraft.DotaCraft;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CommandStartGame implements BasicCommand {

    private final DotaCraft plugin;

    public CommandStartGame(DotaCraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        World world = null;

        if (stack.getSender() instanceof Player player) {
            world = player.getWorld();
        } else {
            world = Bukkit.getWorlds().get(0);
        }

        if (world == null) {
            stack.getSender().sendMessage("§cUnable to resolve world!");
            return;
        }

        plugin.getObjectiveManager().clearAll();
        plugin.getObjectiveManager().setupRadiantStructures(world);

        plugin.getCreepLaneManager().startSpawning(world);

        Bukkit.broadcast(net.kyori.adventure.text.Component.text(
                "§a§l[DotaCraft] §fGame started."
        ));
    }
}