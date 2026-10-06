package com.dotaCraft.Command;

import com.dotaCraft.Manager.NeutralManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CommandSpawnNeutral implements BasicCommand {

    private final NeutralManager neutralManager;

    public CommandSpawnNeutral(NeutralManager neutralManager) {
        this.neutralManager = neutralManager;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage("§cThis command is for players only.");
            return;
        }

        if (args.length == 0) {
            player.sendMessage("§eДоступные нейтралы: §f" + String.join(", ", neutralManager.getRegisteredCreepIds()));
            player.sendMessage("§7Использование: /spawnneutral ");
            return;
        }

        String creepId = args[0].toLowerCase();
        LivingEntity spawned = neutralManager.spawnCreepManual(creepId, player.getLocation());

        if (spawned != null) {
            player.sendMessage("§a[Neutral] Заспавнен нейтральный крип: §e" + creepId);
        } else {
            player.sendMessage("§c[Neutral] Крип с ID '" + creepId + "' не найден!");
        }
    }
}