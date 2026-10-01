package com.dotaCraft.Hero.impl.pudge;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class MeatHook extends Ability implements org.bukkit.event.Listener {

    public MeatHook() {
        super(
                DamageTypes.PURE,
                AbilityTypes.POINT_TARGET,
                TargetTypes.NONE,
                DispelTypes.NONE,
                new double[]{150, 220, 290, 360}, // damage
                new double[]{120, 120, 120, 120}, // manaCost
                new double[]{0, 0, 0, 0},         // healthCost
                new double[]{18, 16, 14, 12},     // coolDown
                new double[]{30, 35, 40, 45},     // castRange (blocks)
                0,
                new double[]{0, 0, 0, 0},
                new double[]{2, 2, 2, 2},
                1, 4, 1,
                false, false, true
        );
        org.bukkit.Bukkit.getPluginManager().registerEvents(this, DotaCraft.getInstance());
    }

    // fix for leash breaking
    @EventHandler
    public void onLeashBreak(EntityUnleashEvent event) {
        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE) {
            event.setCancelled(true);
        }
    }

    @Override
    public void cast(Hero hero) {
        Player player = hero.getPlayer();
        Location startLoc = player.getEyeLocation();
        Vector direction = startLoc.getDirection().normalize();

        int level = getAbilityLevel();
        double maxDistance = getCastRange(level);
        //double damage = getDamage(level); - temporary
        double damage = 0;

        Location spawnLoc = startLoc.clone().subtract(0, 2.5, 0);

        ArmorStand hookHead = player.getWorld().spawn(spawnLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.getEquipment().setHelmet(new ItemStack(Material.TRIPWIRE_HOOK));
        });

        Slime leashHolder = player.getWorld().spawn(spawnLoc.clone().add(0, 1.5, 0), Slime.class, slime -> {
            slime.setSize(0);
            slime.setSilent(true);
            slime.setGravity(false);
            slime.setAI(false);
            slime.setInvulnerable(true);
            slime.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false
            ));
            slime.setLeashHolder(player);
        });

        player.getWorld().playSound(player.getLocation(), "dotacraft:pudge.hook_cast", 0.8f, 1.0f);

        new BukkitRunnable() {
            private double currentDistance = 0;
            private boolean returning = false;
            private LivingEntity hookedTarget = null;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    removeEntities();
                    cancel();
                    return;
                }

                Location currentHookLoc = hookHead.getLocation();

                if (!returning) {
                    currentDistance += 1.2;
                    Location nextLoc = currentHookLoc.add(direction.clone().multiply(1.2));

                    hookHead.teleport(nextLoc);
                    leashHolder.teleport(nextLoc.clone().add(0, 1.5, 0));

                    for (Entity entity : nextLoc.getWorld().getNearbyEntities(nextLoc, 1.2, 1.2, 1.2)) {
                        if (entity instanceof LivingEntity && !entity.equals(player) && !entity.equals(hookHead) && !entity.equals(leashHolder)) {
                            hookedTarget = (LivingEntity) entity;
                            returning = true;

                            player.stopSound("dotacraft:pudge.hook_cast");

                            player.playSound(player.getLocation(), "dotacraft:pudge.hook_impact", 0.5f, 1.0f);

                            player.getWorld().playSound(player.getLocation(), "dotacraft:pudge.hook_cast", 0.8f, 1.0f);

                            hookedTarget.damage(damage, player);
                            break;
                        }
                    }

                    if (currentDistance >= maxDistance) {
                        returning = true;
                    }

                } else {
                    Location pLoc = player.getEyeLocation().subtract(0, 2, 0);
                    Vector toPlayer = pLoc.toVector().subtract(hookHead.getLocation().toVector());

                    if (toPlayer.length() < 1.5) {
                        removeEntities();
                        cancel();
                        return;
                    }

                    toPlayer.normalize().multiply(1.2);
                    Location nextLoc = hookHead.getLocation().add(toPlayer);

                    hookHead.teleport(nextLoc);
                    leashHolder.teleport(nextLoc.clone().add(0, 1.5, 0));

                    if (hookedTarget != null && !hookedTarget.isDead()) {
                        hookedTarget.teleport(nextLoc.clone().add(0, 0.5, 0));
                        hookedTarget.setFallDistance(0);
                    }
                }
            }

            private void removeEntities() {
                player.stopSound("dotacraft:pudge.hook_cast");

                if (leashHolder != null && leashHolder.isValid()) {
                    leashHolder.setLeashHolder(null);

                    Location loc = leashHolder.getLocation();
                    leashHolder.remove();

                    for (Entity entity : loc.getWorld().getNearbyEntities(loc, 2, 2, 2)) {
                        if (entity instanceof Item item) {
                            if (item.getItemStack().getType() == Material.LEAD) {
                                item.remove();
                            }
                        }
                    }
                }
                if (hookHead != null && hookHead.isValid()) {
                    hookHead.remove();
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
    }
}