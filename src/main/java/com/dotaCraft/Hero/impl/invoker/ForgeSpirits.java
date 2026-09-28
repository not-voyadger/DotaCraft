package com.dotaCraft.Hero.impl.invoker;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ForgeSpirits extends Ability {
    public ForgeSpirits() {
        super(
                DamageTypes.NONE,
                AbilityTypes.NO_TARGET,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[]{0, 0, 0, 0},
                new double[]{75, 75, 75, 75},
                new double[]{0, 0, 0, 0},
                new double[]{27, 27, 27, 27},
                new double[]{0, 0, 0, 0},
                0,
                new double[]{0, 0, 0, 0},
                new double[]{10, 20, 30, 40, 50, 60, 70, 80, 90, 100},
                1,
                7,
                2,
                false,
                false,
                false
        );
    }

    @Override
    public void cast(Hero hero) {
        Player player = hero.getPlayer();
        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();

        Location spawnLoc = startLoc.clone().subtract(0, 2.5, 0);

        player.spawnParticle(Particle.FLAME, spawnLoc, 50, 0.5, 1.0, 0.5, 0.1);
        player.spawnParticle(Particle.LAVA, spawnLoc, 20, 0.5, 1.0, 0.5);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

        direction.setY(0).normalize();
        Vector leftVector = new Vector(-direction.getZ(), 0, direction.getX()).multiply(1.5);
        Vector rightVector = new Vector(direction.getZ(), 0, -direction.getX()).multiply(1.5);

        Location leftSpawn = spawnLoc.clone().add(leftVector);
        Location rightSpawn = spawnLoc.clone().add(rightVector);

        spawnForgeSpirit(player, leftSpawn);
        spawnForgeSpirit(player, rightSpawn);
    }

    private void spawnForgeSpirit(Player owner, Location loc) {
        Blaze spirit = (Blaze) loc.getWorld().spawnEntity(loc, EntityType.BLAZE);

        spirit.setCustomName("§cForge Spirit");
        spirit.setCustomNameVisible(true);

        spirit.setRemoveWhenFarAway(false);
    }
}
