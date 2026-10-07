package com.dotaCraft;

import com.dotaCraft.Command.CommandDotaTest;
import com.dotaCraft.Command.CommandSpawnNeutral;
import com.dotaCraft.Listeners.AbilityListener;
import com.dotaCraft.Manager.*;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class DotaCraft extends JavaPlugin {

    private static DotaCraft instance;
    private HeroManager heroManager;
    private HeroBarManager heroBarManager;
    private NeutralManager neutralManager;
    private VisionManager visionManager;

    @Override
    public void onEnable() {
        instance = this;

        this.heroBarManager = new HeroBarManager();
        this.heroManager = new HeroManager(this.heroBarManager);
        this.neutralManager = new NeutralManager();
        this.visionManager = new VisionManager();

        getServer().getPluginManager().registerEvents(new AbilityListener(heroManager), this);
        getServer().getPluginManager().registerEvents(new AttackManager(), this);
        getServer().getPluginManager().registerEvents(new VisionManager(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register("dotatest", "Test command", new CommandDotaTest(heroManager));
            event.registrar().register("spawnneutral", "Spawn neutral creep command", new CommandSpawnNeutral(neutralManager));
        });

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var hero = HeroManager.getHero(player);
                if (hero != null) {
                    hero.onTick();
                    heroBarManager.updateBars(player, hero);
                }
            }
        }, 0L, 1L);

        getLogger().info("DotaCraft successfully enabled!");
    }

    @Override
    public void onDisable() {
        if (heroManager != null) {
            heroManager.clear();
        }

        getLogger().info("DotaCraft disabled.");
    }

    public static DotaCraft getInstance() {
        return instance;
    }

    public HeroManager getHeroManager() {
        return heroManager;
    }

    public NeutralManager getNeutralManager() {
        return neutralManager;
    }
}