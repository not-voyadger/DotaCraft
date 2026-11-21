package com.dotaCraft.Hero.impl.axe;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CullingBlade extends Ability {

    public static final String ICON = ""; //TODO
    public static final String STACKS_ICON = "";
    private int stackCount = 0;
    Particle.DustOptions redDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.6f);


    public CullingBlade() {
        super(
                DamageTypes.PURE,
                AbilityTypes.UNIT_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.NONE,
                new double[] {275, 375, 475},
                new double[] {100, 120, 150},
                new double[] {0, 0, 0},
                new double[] {80, 75, 70},
                new double[] {175, 175, 175},
                0.3,
                new double[] {0, 0, 0},
                new double[] {0, 0, 0},
                1,
                3,
                6,
                false,
                false,
                false,
                ICON
        );
    }

    @Override
    public boolean cast(Hero hero) {
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

        startCastWithPoint(hero, () -> {

            if (!targetEntity.isValid() || targetEntity.isDead() || !targetEntity.getWorld().equals(player.getWorld())) {
                return;
            }

            double maxAllowedDistance = range + 2.0;
            if (player.getLocation().distanceSquared(targetEntity.getLocation()) > maxAllowedDistance * maxAllowedDistance) {
                return;
            }

            DamageManager.dealDamageFromAbility(hero, targetEntity, getDamage(getAbilityLevel()), getDamageType());

        }, null);



        if (targetEntity == hero || targetEntity.isDead()) {
            this.resetCooldown();
            stackCount++;
            IconManager.spawnPositiveIconWithStack(player, STACKS_ICON, stackCount);

            Location loc = targetEntity.getLocation().add(0, 0.2, 0);
            targetEntity.getWorld().spawnParticle(Particle.DUST, loc, 100, 1, 1, 1, 0.1, redDust);
            //successful sound
        }

        //normal sound
        return true;
    }
}
