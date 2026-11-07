package com.dotaCraft.Hero.impl.lion;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.EffectManager;
import com.dotaCraft.Manager.HologramManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class EarthSpike extends Ability {

    public static final String ICON = "\uE500";

    public EarthSpike() {
        super(
                DamageTypes.MAGICAL,
                AbilityTypes.POINT_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.STRONG,
                new double[]{105, 170, 235, 300},
                new double[]{90, 110, 130, 150},
                new double[]{0, 0, 0, 0},
                new double[]{14, 13, 12, 11},
                new double[]{575, 650, 725, 800},
                0,
                new double[]{125, 125, 125, 125},
                new double[]{1.3, 1.6, 1.9, 2.2},
                1, 4, 1,
                false, false, false, ICON
        );
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();
        if (player == null || !player.isOnline()) return false;

        int level = getAbilityLevel();
        double maxDistance = getCastRange(level);
        double radius = getEffectRadius(level);
        double damage = getDamage(level);
        double stunDuration = getDuration(level);

        Location startLoc = player.getLocation();
        World world = player.getWorld();

        Vector stepVector = startLoc.getDirection().setY(0).normalize().multiply(1.2);
        Vector tossVector = new Vector(0, 0.65, 0).add(stepVector.clone().normalize().multiply(0.15));

        world.playSound(startLoc, "dotacraft:lion.lion_earth_spike_cast", org.bukkit.SoundCategory.PLAYERS, 0.8f, 1.0f);

        final Set<Entity> hitEntities = new HashSet<>();

        new BukkitRunnable() {
            private double currentDistance = 0;
            private final Location currentLoc = startLoc.clone();

            @Override
            public void run() {
                if (!player.isOnline() || currentDistance >= maxDistance) {
                    cancel();
                    return;
                }

                currentLoc.add(stepVector);
                currentDistance += 1.2;

                Block targetBlock = getSurfaceBlock(currentLoc);
                if (targetBlock == null) return;

                Location spikeLoc = targetBlock.getLocation();

                spawnSpikeBlock(targetBlock);

                world.spawnParticle(Particle.BLOCK, spikeLoc.add(0.5, 1, 0.5), 15, 0.3, 0.3, 0.3, targetBlock.getBlockData());

                BoundingBox searchBox = BoundingBox.of(spikeLoc, radius, 2.5, radius);

                for (Entity entity : world.getNearbyEntities(searchBox)) {
                    if (entity instanceof LivingEntity victim
                            && !(entity instanceof ArmorStand)
                            && !entity.equals(player)
                            && hitEntities.add(entity)) {

                        DamageManager.dealDamageFromAbility(hero, victim, damage, getDamageType());
                        EffectManager.applyStun(victim, stunDuration);
                        HologramManager.spawnStunIndicator(victim, stunDuration);

                        world.playSound(spikeLoc, "dotacraft:lion.lion_earth_spike_impact", org.bukkit.SoundCategory.PLAYERS, 0.8f, 1.0f);
                        victim.setVelocity(tossVector);
                    }
                }
            }

            private Block getSurfaceBlock(Location loc) {
                Block b = loc.getBlock();
                for (int i = 0; i < 4; i++) {
                    if (b.getType() != Material.AIR && b.getRelative(BlockFace.UP).getType() == Material.AIR) {
                        return b.getRelative(BlockFace.UP);
                    }
                    b = b.getRelative(BlockFace.DOWN);
                }
                return loc.getBlock();
            }

            private void spawnSpikeBlock(Block block) {
                if (block.getType() != Material.AIR) return;

                BlockData originalData = block.getBlockData();
                block.setType(Material.POINTED_DRIPSTONE, false);

                if (block.getBlockData() instanceof Directional directional) {
                    directional.setFacing(BlockFace.UP);
                    block.setBlockData(directional, false);
                }

                Location spawnLoc = block.getLocation().add(0.5, 0.5, 0.5);

                world.spawnParticle(Particle.LAVA, spawnLoc, 4, 0.2, 0.2, 0.2, 0.0);
                world.spawnParticle(Particle.FLAME, spawnLoc, 8, 0.25, 0.25, 0.25, 0.05);

                DotaCraft.getInstance().getServer().getScheduler().runTaskLater(DotaCraft.getInstance(), () -> {
                    if (block.getType() == Material.POINTED_DRIPSTONE) {
                        block.setBlockData(originalData, false);
                        world.spawnParticle(Particle.SMOKE, spawnLoc, 5, 0.2, 0.2, 0.2, 0.02);
                    }
                }, 18L);
            }

        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);

        return true;
    }
}