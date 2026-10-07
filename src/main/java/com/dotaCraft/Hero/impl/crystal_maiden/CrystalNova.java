package com.dotaCraft.Hero.impl.crystal_maiden;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class CrystalNova extends Ability {

    public CrystalNova() {
        super(
                DamageTypes.MAGICAL,
                AbilityTypes.POINT_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.BASIC,
                new double[] {110, 160, 210, 260},
                new double[] {115, 135, 155, 175},
                new double[] {0, 0, 0, 0},
                new double[] {11, 10, 9, 8},
                new double[] {700, 700, 700, 700},
                0.3,
                new double[] {425, 425, 425, 425},
                new double[] {5, 5, 5, 5},
                1, 4, 1,
                false, false, false
        );
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();
        if (player == null || !player.isOnline()) return false;

        int level = getAbilityLevel();
        double castRange = getCastRange(level);
        double radius = getEffectRadius(level);
        double damage = getDamage(level);
        double durationSeconds = getDuration(level);
        int durationTicks = (int) (durationSeconds * 20);

        Location targetLoc;
        var targetBlock = player.getTargetBlockExact((int) castRange);
        if (targetBlock != null) {
            targetLoc = targetBlock.getLocation().add(0.5, 1.0, 0.5);
        } else {
            targetLoc = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(castRange));
        }

        World world = targetLoc.getWorld();
        if (world == null) return false;

        world.playSound(targetLoc, Sound.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH, 1.5f, 1.5f);

        drawNovaEffect(targetLoc, radius);

        spawnTemporaryVision(targetLoc);

        Set<LivingEntity> targets = new HashSet<>();
        for (Entity entity : world.getNearbyEntities(targetLoc, radius, 3.0, radius)) {
            if (entity instanceof LivingEntity victim
                    && !(entity instanceof ArmorStand)
                    && !entity.equals(player)) {

                targets.add(victim);
            }
        }

        for (LivingEntity victim : targets) {
            DamageManager.dealDamageFromAbility(hero, victim, damage, getDamageType());

            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 1, false, true));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, durationTicks, 1, false, true));
        }

        return true;
    }

    private void drawNovaEffect(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.SNOWFLAKE, center, 120, radius / 2, 0.5, radius / 2, 0.15);
        world.spawnParticle(Particle.ITEM_SNOWBALL, center, 80, radius / 2, 0.8, radius / 2, 0.1);
        world.spawnParticle(Particle.INSTANT_EFFECT, center, 40, radius / 3, 0.3, radius / 3, 0.05);

        int points = 36;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLoc = center.clone().add(x, 0.2, z);
            world.spawnParticle(Particle.SNOWFLAKE, particleLoc, 2, 0.05, 0.1, 0.05, 0.01);
            world.spawnParticle(Particle.FIREWORK, particleLoc, 1, 0.0, 0.0, 0.0, 0.01);
        }
    }

    private void spawnTemporaryVision(Location loc) {
        ArmorStand visionMarker = loc.getWorld().spawn(loc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvulnerable(true);
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                if (visionMarker.isValid()) {
                    visionMarker.remove();
                }
            }
        }.runTaskLater(DotaCraft.getInstance(), 80L);
    }
}