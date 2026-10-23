package com.dotaCraft.Hero.impl.lion;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.EffectManager;
import org.bukkit.entity.Frog;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Hex extends Ability {
    public Hex() {
        super(
                DamageTypes.NONE,
                AbilityTypes.UNIT_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.STRONG,
                new double []{0,0,0,0},
                new double[]{110, 140, 170, 200},
                new double []{0,0,0,0},
                new double []{24,20,16,12},
                new double[]{500, 525, 550, 575},
                0,
                new double []{0,0,0,0},
                new double []{2,2.4,2.8,3.2},
                1,
                4,
                1,
                false,
                false,
                false);
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

        double durationSeconds = this.getDuration(hero.getLevel());

        EffectManager.applyHex(
                targetEntity,
                durationSeconds,
                Frog.class,
                "dotacraft:lion.lion_hex_cast",
                null
        );

        return true;
    }
}