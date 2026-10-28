package com.dotaCraft;

import com.dotaCraft.Command.CommandDotaTest;
import com.dotaCraft.Command.CommandSpawnNeutral;
import com.dotaCraft.Command.CommandStartGame;
import com.dotaCraft.Listeners.AbilityListener;
import com.dotaCraft.Listeners.ChatListener;
import com.dotaCraft.Listeners.CourierListener;
import com.dotaCraft.Command.CommandSpawnLaneCreep;
import com.dotaCraft.Manager.*;
import com.dotaCraft.Objective.Objective;
import com.dotaCraft.Objective.Tower;
import com.dotaCraft.UI.MainHUD;
import com.sun.source.tree.Tree;
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
    private WardManager wardManager;
    private MainHUD mainHUD;
    private TreeManager treeManager;
    private ObjectiveManager objectiveManager;
    private HealthBarManager healthBarManager;
    private CreepLaneManager creepLaneManager;

    @Override
    public void onEnable() {
        instance = this;

        this.heroBarManager = new HeroBarManager();
        this.heroManager = new HeroManager(this.heroBarManager);
        this.neutralManager = new NeutralManager();
        this.visionManager = new VisionManager();
        this.wardManager = new WardManager();
        this.mainHUD = new MainHUD(this.heroManager);
        this.treeManager = new TreeManager();
        this.objectiveManager = new ObjectiveManager();
        this.healthBarManager = new HealthBarManager();
        this.creepLaneManager = new CreepLaneManager(this);

        getServer().getPluginManager().registerEvents(new AbilityListener(heroManager), this);
        getServer().getPluginManager().registerEvents(new AttackManager(), this);
        getServer().getPluginManager().registerEvents(this.visionManager, this);
        getServer().getPluginManager().registerEvents(this.treeManager, this);
        getServer().getPluginManager().registerEvents(new CourierListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register("dotatest", "Test command", new CommandDotaTest(heroManager));
            event.registrar().register("spawnneutral", "Spawn neutral creep command", new CommandSpawnNeutral(neutralManager));
            event.registrar().register("spawnlane", "Spawn lane creep command", new CommandSpawnLaneCreep(this));
            event.registrar().register("startgame", "Start dota match and setup map", new CommandStartGame(this));
        });

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (objectiveManager != null) {
                objectiveManager.onTick();
            }

            if (creepLaneManager != null) {
                creepLaneManager.onTick();
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                var hero = HeroManager.getHero(player);
                if (hero != null) {
                    hero.onTick();
                    heroBarManager.updateBars(player, hero);
                    mainHUD.updateHUD(player);

                    for (Objective obj : objectiveManager.getActiveObjectives()) {
                        if (obj instanceof Tower tower && tower.isAlive()) {
                            healthBarManager.updateOrCreateBar(
                                    tower.getEntity(),
                                    tower.getLocation().clone().add(0, 7, 0),
                                    tower.getName(),
                                    tower.getCurrentHealth(),
                                    tower.getMaxHealth(),
                                    3.0f,
                                    player,
                                    tower.getTeamName()
                            );
                        }
                    }
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

        if (objectiveManager != null) {
            objectiveManager.clearAll();
        }

        if (healthBarManager != null) {
            healthBarManager.clearAll();
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

    public WardManager getWardManager() { return wardManager; }

    public MainHUD getMainHUD() { return mainHUD; }

    public TreeManager getTreeManager() { return treeManager; }

    public ObjectiveManager getObjectiveManager() { return objectiveManager; }

    public HealthBarManager getHealthBarManager() { return healthBarManager; }

    public CreepLaneManager getCreepLaneManager() { return creepLaneManager; }
}