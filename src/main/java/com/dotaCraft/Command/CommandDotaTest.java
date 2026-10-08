package com.dotaCraft.Command;

import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.impl.*;
import com.dotaCraft.Manager.HeroManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;

public class CommandDotaTest implements BasicCommand {

    private final HeroManager heroManager;

    public CommandDotaTest(HeroManager heroManager) {
        this.heroManager = heroManager;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage("Command is for players only");
            return;
        }

        if (args.length == 0) {
            player.sendMessage("§eAvailable heroes: §f" + String.join(", ", HeroManager.getAvailableHeroIds()));
            return;
        }

        String heroId = args[0].toLowerCase();
        Hero hero = heroManager.createAndRegisterHero(heroId, player);

        if (hero == null) {
            player.sendMessage("§cHero '" + heroId + "' was not found.");
            return;
        }

        if (heroId.equals("pudge")) {
            hero.addItem(0, new ScytheOfVyse());
            hero.addItem(1, new MagicStick());
            hero.addItem(2, new PhaseBoots());
            hero.addItem(3, new Daedalus());
        }

        if (heroId.equals("invoker")) {
            hero.addItem(0, new ScytheOfVyse());
            hero.addItem(1, new MagicStick());
        }

        if (heroId.equals("lion")) {
            hero.addItem(1, new MagicStick());
        }

        if (heroId.equals("crystal_maiden")) {
            hero.addItem(1, new ObserverWardItem());
        }

        player.sendMessage("§aYou've picked : " + hero.getHeroName());
    }
}