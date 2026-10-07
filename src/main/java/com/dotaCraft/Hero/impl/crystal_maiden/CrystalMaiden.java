package com.dotaCraft.Hero.impl.crystal_maiden;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class CrystalMaiden extends Hero {
    public CrystalMaiden(Player player) {
        super(
                player,
                "Crystal Maiden",
                Attribute.INTELLECT,
                17,
                16,
                20,
                2.0,
                1.0,
                2.2,
                1.8,
                3.3,
                1.7,
                AttackType.RANGED,
                600.0,
                900.0,
                48,
                54,
                280.0
        );

        addAbility(1, new Frostbite());
    }

    @Override
    public void launchProjectile(Hero attacker, LivingEntity target, double damage) {
        Player player = attacker.getPlayer();

        new BukkitRunnable() {
            private final Location currentLoc = player.getEyeLocation().clone().subtract(0, 0.2, 0);
            private final double blocksPerTick = attacker.getProjectileSpeed() / 20.0;
            private int maxTicks = 60;

            @Override
            public void run() {
                maxTicks--;

                if (target == null || !target.isValid() || target.isDead() || maxTicks <= 0) {
                    cancel();
                    return;
                }

                Location targetLoc = target.getLocation().clone().add(0, target.getHeight() / 2.0, 0);
                Vector direction = targetLoc.toVector().subtract(currentLoc.toVector());

                if (direction.length() <= blocksPerTick) {
                    onProjectileHit(target, damage);
                    cancel();
                    return;
                }

                direction.normalize().multiply(blocksPerTick);
                currentLoc.add(direction);

                if (currentLoc.getWorld() != null) {
                    currentLoc.getWorld().spawnParticle(Particle.SNOWFLAKE, currentLoc, 3, 0.03, 0.03, 0.03, 0.01);
                    currentLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, currentLoc, 1, 0.03, 0.03, 0.03, 0.01);
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 1L);
    }
}
