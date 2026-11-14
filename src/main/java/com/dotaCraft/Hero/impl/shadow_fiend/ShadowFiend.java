package com.dotaCraft.Hero.impl.shadow_fiend;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ShadowFiend extends Hero {
    public ShadowFiend(Player player) {
        super(
                player,
                "shadow_fiend",
                Attribute.AGILITY,
                19,
                25,
                16,
                2.15,
                1.1,
                2.7,
                3.6,
                2.2,
                1.6,
                AttackType.RANGED,
                800,
                1200,
                16,
                22,
                305
        );

        this.setInnateAbility(new Necromastery());

        this.addAbility(0, new ShadowrazeNear());
        this.addAbility(1, new ShadowrazeMedium());
        this.addAbility(2, new ShadowrazeFar());
        this.addAbility(3, new FeastOfSouls());
        this.addAbility(4, new PresenceOfTheDarkLord());
    }

    @Override
    public void launchProjectile(Hero attacker, Entity target, double damage) {
        Player player = attacker.getPlayer();

        player.getWorld().playSound(player.getLocation(), "dotacraft:shadow_fiend.shadow_fiend_projectile", 0.25f, 1.0f);

        final Particle.DustOptions crimsonDust = new Particle.DustOptions(Color.fromRGB(220, 20, 20), 1.1f);
        final Particle.DustOptions darkBloodDust = new Particle.DustOptions(Color.fromRGB(110, 0, 0), 0.85f);
        final Particle.DustOptions shadowDust = new Particle.DustOptions(Color.fromRGB(25, 10, 15), 0.7f);

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
                    if (target.getWorld() != null) {
                        target.getWorld().spawnParticle(Particle.DUST, targetLoc, 8, 0.2, 0.2, 0.2, 0.0, crimsonDust);
                        target.getWorld().spawnParticle(Particle.CRIMSON_SPORE, targetLoc, 5, 0.15, 0.15, 0.15, 0.02);
                    }
                    onProjectileHit(target, damage);
                    cancel();
                    return;
                }

                direction.normalize().multiply(blocksPerTick);
                currentLoc.add(direction);

                if (currentLoc.getWorld() != null) {
                    currentLoc.getWorld().spawnParticle(Particle.DUST, currentLoc, 2, 0.02, 0.02, 0.02, 0.0, crimsonDust);
                    currentLoc.getWorld().spawnParticle(Particle.DUST, currentLoc, 3, 0.06, 0.06, 0.06, 0.0, darkBloodDust);
                    currentLoc.getWorld().spawnParticle(Particle.DUST, currentLoc, 2, 0.08, 0.08, 0.08, 0.0, shadowDust);
                    currentLoc.getWorld().spawnParticle(Particle.SMOKE, currentLoc, 1, 0.02, 0.02, 0.02, 0.005);
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 1L);
    }
}
