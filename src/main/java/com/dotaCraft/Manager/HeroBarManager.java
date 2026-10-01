package com.dotaCraft.Manager;

import com.dotaCraft.Hero.Hero;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HeroBarManager {

    private static class HeroBars {
        private final BossBar healthBar;
        private final BossBar manaBar;

        public HeroBars(BossBar healthBar, BossBar manaBar) {
            this.healthBar = healthBar;
            this.manaBar = manaBar;
        }
    }

    private final Map<UUID, HeroBars> playerBars = new HashMap<>();

    public void createBars(Player player, Hero hero) {
        removeBars(player); // Clean old stuff

        // Healthbar
        BossBar healthBar = Bukkit.createBossBar(
                getHealthTitle(hero),
                BarColor.RED,
                BarStyle.SOLID
        );

        // Manabar
        BossBar manaBar = Bukkit.createBossBar(
                getManaTitle(hero),
                BarColor.BLUE,
                BarStyle.SOLID
        );

        healthBar.addPlayer(player);
        manaBar.addPlayer(player);

        healthBar.setVisible(true);
        manaBar.setVisible(true);

        playerBars.put(player.getUniqueId(), new HeroBars(healthBar, manaBar));
        updateBars(player, hero);
    }

    public void updateBars(Player player, Hero hero) {
        HeroBars bars = (HeroBars) playerBars.get(player.getUniqueId());
        if (bars == null) return;

        // Health progress
        double healthProgress = Math.max(0.0, Math.min(1.0, (double) hero.getCurrentHealth() / hero.getMaxHealth()));
        bars.healthBar.setProgress(healthProgress);
        bars.healthBar.setTitle(getHealthTitle(hero));

        // Mana progress
        double manaProgress = Math.max(0.0, Math.min(1.0, (double) hero.getCurrentMana() / hero.getMaxMana()));
        bars.manaBar.setProgress(manaProgress);
        bars.manaBar.setTitle(getManaTitle(hero));
    }

    public void removeBars(Player player) {
        HeroBars bars = (HeroBars) playerBars.remove(player.getUniqueId());
        if (bars != null) {
            bars.healthBar.removeAll();
            bars.manaBar.removeAll();
        }
    }

    public void clearAll() {
        for (HeroBars bars : playerBars.values()) {
            bars.healthBar.removeAll();
            bars.manaBar.removeAll();
        }
        playerBars.clear();
    }

    private String getHealthTitle(Hero hero) {
        return String.format("§c§lHP: §f%d / %d", hero.getCurrentHealth(), hero.getMaxHealth());
    }

    private String getManaTitle(Hero hero) {
        return String.format("§b§lMANA: §f%d / %d", hero.getCurrentMana(), hero.getMaxMana());
    }
}