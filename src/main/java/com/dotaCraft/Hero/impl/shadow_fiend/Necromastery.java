package com.dotaCraft.Hero.impl.shadow_fiend;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Necromastery extends Ability {

    public static final String ICON = ""; // TODO: иконка в панели скиллов
    public static final String BUFF_ICON = "\uE506";

    private static final int[] MAX_SOULS = {20, 20, 20, 20};
    private static final double[] DAMAGE_PER_SOUL = {1.0, 2.0, 3.0, 4.0};

    private int currentSouls = 0;
    private double appliedBonusDamage = 0.0;

    public Necromastery() {
        super(
                DamageTypes.NONE,
                AbilityTypes.PASSIVE,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[]{0, 0, 0, 0},
                new double[]{0, 0, 0, 0},
                new double[]{0, 0, 0, 0},
                new double[]{0, 0, 0, 0},
                new double[]{0, 0, 0, 0},
                0,
                new double[]{0, 0, 0, 0},
                new double[]{0, 0, 0, 0},
                1, 4, 1,
                true,   // isInnate
                true,           // hasScepterUpgrade
                false,
                ICON
        );
    }

    @Override
    public void onLearn(Hero hero) {
        updateBuffIcon(hero);
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();
        if (player != null) {
            player.sendMessage("§c[Necromastery] §7Current souls: §f" + currentSouls + " / " + getMaxSouls());
        }
        return false;
    }

    @Override
    public void onUnitDeath(Hero owner, LivingEntity victim, Hero killer, double distanceBlocks) {
        if (!owner.equals(killer)) return;

        int soulsToAdd = (victim instanceof Player) ? 5 : 1;
        addSouls(owner, soulsToAdd);
    }

    public void addSouls(Hero hero, int amount) {
        if (amount <= 0) return;

        currentSouls = Math.min(getMaxSouls(), currentSouls + amount);
        recalculateBonusDamage(hero);
        updateBuffIcon(hero);

        Player player = hero.getPlayer();
        if (player != null && player.isOnline()) {
            player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0, 1.2, 0), 6, 0.3, 0.4, 0.3, 0.03);
        }
    }

    public void onHeroDeath(Hero hero) {
        currentSouls = currentSouls / 2;
        recalculateBonusDamage(hero);
        updateBuffIcon(hero);
    }

    public void recalculateBonusDamage(Hero hero) {
        if (appliedBonusDamage > 0) {
            hero.setMainDamage(-appliedBonusDamage);
            appliedBonusDamage = 0;
        }

        int level = getAbilityLevel();
        if (level <= 0) return;

        double dmgPerSoul = DAMAGE_PER_SOUL[Math.min(level - 1, DAMAGE_PER_SOUL.length - 1)];
        appliedBonusDamage = currentSouls * dmgPerSoul;

        if (appliedBonusDamage > 0) {
            hero.setMainDamage(appliedBonusDamage);
        }
    }

    private void updateBuffIcon(Hero hero) {
        Player player = hero.getPlayer();
        if (player == null || !player.isOnline()) return;

        IconManager.spawnPositiveIconWithStack(player, BUFF_ICON, currentSouls);
    }

    public int getCurrentSouls() {
        return currentSouls;
    }

    public int getMaxSouls() {
        int level = Math.max(1, getAbilityLevel());
        return MAX_SOULS[Math.min(level - 1, MAX_SOULS.length - 1)];
    }
}