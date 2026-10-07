package com.dotaCraft.Hero.impl.crystal_maiden;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HologramManager;
import com.dotaCraft.Units.NeutralCreep;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Frostbite extends Ability {

    public Frostbite() {
        super(
                DamageTypes.MAGICAL,
                AbilityTypes.UNIT_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.BASIC,
                new double[] {100, 100, 100, 100}, // damage per second!!
                new double[] {125, 135, 145, 155},
                new double[] {0, 0, 0, 0},
                new double[] {9, 8, 7, 6},
                new double[] {600, 600, 600, 600},
                0.3,
                new double[] {0, 0, 0, 0},
                new double[] {1.5, 2.0, 2.5, 3.0},
                1,
                4,
                1,
                false,
                false,
                false
        );
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();

        int level = getAbilityLevel();
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

        NeutralCreep neutralCreep = DotaCraft.getInstance().getNeutralManager().getActiveCreep(targetEntity.getUniqueId());
        double baseDamage = this.getDamage(level) / 4;
        final double damageForTick = (neutralCreep != null)
                ? baseDamage * 4.0
                : baseDamage;

        double durationSeconds = this.getDuration(hero.getLevel());
        int durationTicks = (int) (durationSeconds * 20);
        boolean isFrozen = false;

        player.getWorld().playSound(player.getLocation(), "dotacraft:crystal_maiden.crystal_maiden_frostbite_cast", 0.5f, 1.0f);

        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 255, false, false));
        targetEntity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 255, false, false));

        HologramManager.spawnRootedIndicator(targetEntity, durationSeconds);
        spawnFrozenBlocks(targetEntity.getLocation(), targetEntity, player);

        new BukkitRunnable() {
            int ticksLived = 0;

            @Override
            public void run() {
                if (!targetEntity.isValid() || targetEntity.isDead() || ticksLived >= durationTicks) {
                    cancel();
                    return;
                }

                if (ticksLived % 5 == 0) {
                    DamageManager.dealDamageFromAbility(hero, targetEntity, damageForTick, getDamageType());
                }

                ticksLived++;
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
        return true;
    }

    private void spawnFrozenBlocks(Location victimLocation, LivingEntity targetEntity, Player player) {

        if (victimLocation.getWorld() == null) return;

        victimLocation.getWorld().playSound(victimLocation, org.bukkit.Sound.BLOCK_GLASS_BREAK, 1.0f, 0.5f);
        victimLocation.getWorld().playSound(victimLocation, org.bukkit.Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 0.8f);

        int iceCount = 4;
        int durationTicks = (int) (this.getDuration(getAbilityLevel()) * 20);

        for (int i = 0; i < iceCount; i++) {
            final int index = i;

            double angle = index * (2 * Math.PI / iceCount);
            double offsetX = 0.4 * Math.cos(angle);
            double offsetZ = 0.4 * Math.sin(angle);

            Location standLoc = victimLocation.clone().add(offsetX, -0.5, offsetZ);

            ArmorStand stand = victimLocation.getWorld().spawn(standLoc, ArmorStand.class, armorStand -> {
                armorStand.setVisible(false);
                armorStand.setGravity(false);
                armorStand.setSmall(true);
                armorStand.setMarker(true);

                org.bukkit.inventory.ItemStack iceItem = new org.bukkit.inventory.ItemStack(
                        index % 2 == 0 ? org.bukkit.Material.ICE : org.bukkit.Material.BLUE_ICE
                );
                if (armorStand.getEquipment() != null) {
                    armorStand.getEquipment().setHelmet(iceItem);
                }
            });

            new BukkitRunnable() {
                int ticksLived = 0;

                @Override
                public void run() {
                    ticksLived++;

                    if (targetEntity == null || !targetEntity.isValid() || targetEntity.isDead() || ticksLived >= durationTicks) {
                        if (stand.isValid()) {
                            stand.getWorld().spawnParticle(
                                    org.bukkit.Particle.BLOCK,
                                    stand.getLocation().add(0, 0.5, 0),
                                    10, 0.1, 0.1, 0.1,
                                    org.bukkit.Material.ICE.createBlockData()
                            );
                            //stand.getWorld().playSound(stand.getLocation(), org.bukkit.Sound.BLOCK_GLASS_BREAK, 0.5f, 1.5f);
                            stand.remove();
                            player.stopSound("dotacraft:crystal_maiden.crystal_maiden_frostbite_cast");
                        }
                        cancel();
                    }
                }
            }.runTaskTimer(DotaCraft.getInstance(), 1L, 1L);
        }

        victimLocation.getWorld().spawnParticle(
                org.bukkit.Particle.SNOWFLAKE,
                victimLocation.clone().add(0, 1, 0),
                40, 0.4, 0.8, 0.4, 0.05
        );

        victimLocation.getWorld().spawnParticle(
                org.bukkit.Particle.BLOCK,
                victimLocation.clone().add(0, 0.5, 0),
                30, 0.3, 0.5, 0.3,
                org.bukkit.Material.ICE.createBlockData()
        );
    }
}
