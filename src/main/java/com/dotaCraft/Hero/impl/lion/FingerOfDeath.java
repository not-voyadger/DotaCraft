package com.dotaCraft.Hero.impl.lion;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HologramManager;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FingerOfDeath extends Ability {
    private double[] additionalDamage = {20, 30, 40};
    private int charges = 0;
    private final double DAMAGE_PER_KILL = 25.0;

    public FingerOfDeath() {
        super(DamageTypes.MAGICAL, AbilityTypes.UNIT_TARGET, TargetTypes.ENEMY, DispelTypes.NONE, new double[]{600, 725, 850}, new double[]{200, 400, 600}, new double[]{0, 0, 0}, new double[]{140, 90, 40}, new double[]{900, 900, 900}, 0.3, new double[]{0, 0, 0}, new double[]{0, 0, 0}, 1, 3, 6, false, true, false);
    }

    private double additionalDamageBasedOnAbilityLevel(int currentLevel) {
        if (currentLevel <= 0) return additionalDamage[0];

        if (currentLevel >= additionalDamage.length) {
            return 0;
        }

        return additionalDamage[currentLevel - 1];
    }

    @Override
    public double getDamage(int level) {
        return super.getDamage(level) + (charges * DAMAGE_PER_KILL);
    }

    public void addKill() {
        this.charges++;
    }

    public int getCharges() {
        return charges;
    }

    @Override
    public boolean cast(Hero hero) {
        hero.resetAttackCooldown();

        int totalMeleeTicks = 400;
        int level = this.getAbilityLevel();
        double totalMeleeBonusDamage = additionalDamageBasedOnAbilityLevel(level) + (charges * DAMAGE_PER_KILL);

        Player player = hero.getPlayer();

        double range = this.getCastRange(hero.getLevel());

        org.bukkit.util.RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.5,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        if (result == null || !(result.getHitEntity() instanceof LivingEntity targetEntity)) {
            return false;
        }

        // Sound and visual design
        player.getWorld().playSound(player.getLocation(), "dotacraft:lion.lion_finger_of_death_cast", 0.8f, 1.0f);
        targetEntity.getWorld().spawnParticle(Particle.EXPLOSION, targetEntity.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3);
        drawFingerOfDeathBeam(player.getEyeLocation().subtract(0, 0.3, 0), targetEntity.getEyeLocation().subtract(0, 0.2, 0));

        double spellDamage = this.getDamage(level);
        DamageManager.dealDamage(hero, targetEntity, spellDamage, DamageTypes.MAGICAL);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (targetEntity.isDead() || targetEntity.getHealth() <= 0) {
                    addKill();
                    player.sendMessage("§a[Debug] Killed with finger of death. Current charges: " + charges);
                }
            }
        }.runTaskLater(DotaCraft.getInstance(), 1L);

        new BukkitRunnable() {
            private int currentTick = 0;
            private boolean damageSet = false;

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || currentTick >= totalMeleeTicks) {
                    hero.setAttackType(Hero.AttackType.RANGED);
                    hero.setAttackRange(DotaUnits.toBlocks(600.0));

                    if (damageSet) {
                        hero.setMainDamage(-totalMeleeBonusDamage);
                        damageSet = false;
                    }

                    cancel();
                    return;
                }

                hero.setAttackType(Hero.AttackType.MELEE);
                hero.setAttackRange(DotaUnits.toBlocks(250));

                if (!damageSet) {
                    hero.setMainDamage(totalMeleeBonusDamage);
                    damageSet = true;
                }

                currentTick++;
            }

        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
        return true;
    }

    private void drawFingerOfDeathBeam(Location start, Location end) {
        Vector direction = end.toVector().subtract(start.toVector());
        double distance = direction.length();
        direction.normalize();

        double stepBlue = 0.35;
        Location current = start.clone();

        Particle.DustOptions yellowDust = new Particle.DustOptions(Color.fromRGB(255, 255, 0), 0.6f);

        for (double d = 0; d < distance; d += stepBlue) {
            current.add(direction.clone().multiply(stepBlue));
            start.getWorld().spawnParticle(Particle.DUST, current, 1, 0.0, 0.0, 0.0, 0.0, yellowDust);
            start.getWorld().spawnParticle(Particle.FLAME, current, 3, 0.03, 0.03, 0.03, 0.01);
        }
    }
}
