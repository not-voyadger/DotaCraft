package com.dotaCraft;

import com.dotaCraft.Listeners.AbilityListener;
import com.dotaCraft.Manager.HeroManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class DotaCraft extends JavaPlugin {

    private static DotaCraft instance;
    private HeroManager heroManager;

    @Override
    public void onEnable() {
        instance = this;

        this.heroManager = new HeroManager();

        getServer().getPluginManager().registerEvents(new AbilityListener(heroManager), this);

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
}