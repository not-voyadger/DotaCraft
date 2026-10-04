package com.dotaCraft.Hero.impl.lion;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HologramManager;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class Lion extends Hero {
    public Lion(Player player) {
        super(player, "Lion", Attribute.INTELLECT, 18, 15, 19, 2.3, 0.9, 2.4, 1.7, 3.5, 1.7, AttackType.RANGED, 600.0, 900.0, 29, 35, 290.0);

        this.addAbility(0, new EarthSpike());
        this.addAbility(1, new Hex());
        this.addAbility(2, new ManaDrain());
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
                    Particle.DustOptions goldenDust = new Particle.DustOptions(Color.fromRGB(255, 215, 0), 0.6f);

                    currentLoc.getWorld().spawnParticle(Particle.FLAME, currentLoc, 3, 0.03, 0.03, 0.03, 0.01);
                    currentLoc.getWorld().spawnParticle(Particle.DUST, currentLoc, 1, 0.0, 0.0, 0.0, 0.0, goldenDust);
                    currentLoc.getWorld().spawnParticle(Particle.SMOKE, currentLoc, 1, 0.01, 0.01, 0.01, 0.01);
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 1L);
    }
}
