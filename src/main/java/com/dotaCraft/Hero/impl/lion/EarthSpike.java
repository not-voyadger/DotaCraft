package com.dotaCraft.Hero.impl.lion;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HologramManager;
import org.bukkit.Bukkit;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class EarthSpike extends Ability {

    public EarthSpike() {
        super(
                DamageTypes.MAGICAL,
                AbilityTypes.POINT_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.STRONG,
                new double[]{105, 170, 235, 300}, // damage
                new double[]{90, 110, 130, 150},  // manaCost
                new double[]{0, 0, 0, 0},         // healthCost
                new double[]{14, 13, 12, 11},     // coolDown
                new double[]{575, 650, 725, 800},     // castRange
                0,
                new double[]{125, 125, 125, 125}, // effectRadius
                new double[]{1.3, 1.6, 1.9, 2.2}, // duration (stun)
                1, 4, 1,
                false, false, false
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
        Vector direction = startLoc.getDirection().setY(0).normalize();
        World world = player.getWorld();

        world.playSound(startLoc, "dotacraft:lion.lion_earth_spike_cast", org.bukkit.SoundCategory.PLAYERS, 0.8f, 1.0f);

        final Set hitEntityIds = new HashSet<>();

        new BukkitRunnable() {
            private double currentDistance = 0;
            private final double step = 1.2;
            private Location currentLoc = startLoc.clone();
            private int stepIndex = 0;

            @Override
            public void run() {
                if (!player.isOnline() || currentDistance >= maxDistance) {
                    cancel();
                    return;
                }

                stepIndex++;
                currentLoc.add(direction.clone().multiply(step));
                currentDistance += step;

                Block targetBlock = getSurfaceBlock(currentLoc);
                if (targetBlock == null) return;

                Location spikeLoc = targetBlock.getLocation();

                spawnSpikeBlock(targetBlock);

                world.spawnParticle(Particle.BLOCK, spikeLoc.clone().add(0.5, 1, 0.5), 15, 0.3, 0.3, 0.3, targetBlock.getBlockData());

                for (Entity entity : world.getNearbyEntities(spikeLoc, radius, 2.5, radius)) {
                    if (entity instanceof LivingEntity victim
                            && !(entity instanceof ArmorStand)
                            && !entity.equals(player)
                            && !hitEntityIds.contains(entity.getUniqueId())) {

                        hitEntityIds.add(entity.getUniqueId());

                        DamageManager.dealDamage(hero, victim, damage, getDamageType());
                        applyStun(victim, stunDuration);
                        HologramManager.spawnStunIndicator(victim, stunDuration);

                        Bukkit.getScheduler().runTaskLater(DotaCraft.getInstance(), () -> {
                            if (victim.isValid() && !victim.isDead()) {
                                Vector toss = new Vector(0, 0.65, 0).add(direction.clone().multiply(0.15));
                                world.playSound(spikeLoc, "dotacraft:lion.lion_earth_spike_impact", org.bukkit.SoundCategory.PLAYERS, 0.8f, 1.0f);
                                victim.setVelocity(toss);
                            }
                        }, 1L);
                    }
                }
            }

            private Block getSurfaceBlock(Location loc) {
                Block b = loc.getBlock();
                for (int i = 0; i < 4; i++) {
                    if (!b.getType().isAir() && b.getRelative(0, 1, 0).getType().isAir()) {
                        return b.getRelative(0, 1, 0);
                    }
                    b = b.getRelative(0, -1, 0);
                }
                return loc.getBlock();
            }

            private void spawnSpikeBlock(Block block) {
                if (!block.getType().isAir()) return;

                BlockData originalData = block.getBlockData();

                block.setType(Material.POINTED_DRIPSTONE, false);

                if (block.getBlockData() instanceof Directional directional) {
                    directional.setFacing(BlockFace.UP);
                    block.setBlockData(directional, false);
                }

                Location spawnLoc = block.getLocation().add(0.5, 0.5, 0.5);
                World world = block.getWorld();

                world.spawnParticle(Particle.LAVA, spawnLoc, 4, 0.2, 0.2, 0.2, 0.0);

                world.spawnParticle(Particle.FLAME, spawnLoc, 8, 0.25, 0.25, 0.25, 0.05);

                world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, spawnLoc, 2, 0.1, 0.1, 0.1, 0.02);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (block.getType() == Material.POINTED_DRIPSTONE) {
                            block.setBlockData(originalData, false);

                            // Небольшой эффект дыма/угасания, когда шип прячется обратно
                            world.spawnParticle(Particle.SMOKE, spawnLoc, 5, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                }.runTaskLater(DotaCraft.getInstance(), 18L);
            }

        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
        return true;
    }

    private void applyStun(LivingEntity victim, double durationSeconds) {
        int ticks = (int) (durationSeconds * 20);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 10, false, false, true));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, ticks, 10, false, false, false));
    }
}