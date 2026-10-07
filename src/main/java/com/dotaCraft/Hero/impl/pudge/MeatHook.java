package com.dotaCraft.Hero.impl.pudge;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.inventory.ItemStack;
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
                new double[]{1300, 1300, 1300, 1300}, // castRange (units)
                0,
                new double[]{0, 0, 0, 0},
                new double[]{2, 2, 2, 2},
                1, 4, 1,
                false, false, true
        );
        org.bukkit.Bukkit.getPluginManager().registerEvents(this, DotaCraft.getInstance());
    }

    @EventHandler
    public void onLeashBreak(EntityUnleashEvent event) {
        if (event.getReason() == EntityUnleashEvent.UnleashReason.DISTANCE) {
            event.setCancelled(true);
        }
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();

        Location eyeLoc = player.getEyeLocation();
        int level = getAbilityLevel();
        double maxDistance = getCastRange(level);

        Location targetPoint = eyeLoc.clone().add(eyeLoc.getDirection().multiply(maxDistance));
        Location spawnLoc = eyeLoc.clone().subtract(0, 0.4, 0);

        Vector direction = targetPoint.toVector().subtract(spawnLoc.toVector()).normalize();

        spawnLoc.setDirection(direction);

        double damage = getDamage(level);

        ItemDisplay hookHead = player.getWorld().spawn(spawnLoc, ItemDisplay.class, display -> {
            display.setItemStack(new ItemStack(Material.TRIPWIRE_HOOK));
            display.setTeleportDuration(1);
        });

        Slime leashHolder = player.getWorld().spawn(spawnLoc, Slime.class, slime -> {
            slime.setSize(0);
            slime.setSilent(true);
            slime.setGravity(false);
            slime.setAI(false);
            slime.setInvulnerable(true);
            slime.addPotionEffect(new PotionEffect(
                    PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false
            ));
            slime.setLeashHolder(player);
        });

        player.getWorld().playSound(player.getLocation(), "dotacraft:pudge.hook_cast", 0.8f, 1.0f);

        new BukkitRunnable() {
            private double currentDistance = 0;
            private boolean returning = false;
            private LivingEntity hookedTarget = null;
            private Location currentHookLoc = spawnLoc.clone();

            @Override
            public void run() {
                if (!player.isOnline()) {
                    removeEntities();
                    cancel();
                    return;
                }

                if (!returning) {
                    currentDistance += 1.2;
                    currentHookLoc = currentHookLoc.clone().add(direction.clone().multiply(1.2));

                    hookHead.teleport(currentHookLoc);

                    Location leashLoc = currentHookLoc.clone().add(direction.clone().multiply(-5));
                    leashLoc.add(0, 0.1, 0);

                    leashHolder.teleport(leashLoc.subtract(0, 0.5, 0));

                    for (Entity entity : currentHookLoc.getWorld().getNearbyEntities(currentHookLoc, 1.2, 1.2, 1.2)) {
                        if (entity instanceof LivingEntity target
                                && !entity.equals(player)
                                && !entity.equals(hookHead)
                                && !entity.equals(leashHolder)
                                && !(entity instanceof ArmorStand)) {

                            hookedTarget = target;
                            returning = true;

                            player.stopSound("dotacraft:pudge.hook_cast");
                            player.playSound(player.getLocation(), "dotacraft:pudge.hook_impact", 0.5f, 1.0f);
                            player.getWorld().playSound(player.getLocation(), "dotacraft:pudge.hook_cast", 0.8f, 1.0f);

                            DamageManager.dealDamageFromAbility(hero, hookedTarget, damage, getDamageType());
                            break;
                        }
                    }

                    if (currentDistance >= maxDistance) {
                        returning = true;
                    }

                } else {
                    Location pLoc = player.getEyeLocation().subtract(0, 0.4, 0);
                    Vector toPlayer = pLoc.toVector().subtract(currentHookLoc.toVector());

                    if (toPlayer.length() < 1.2) {
                        removeEntities();
                        cancel();
                        return;
                    }

                    Vector returnDir = toPlayer.normalize().multiply(1.2);
                    currentHookLoc = currentHookLoc.clone().add(returnDir);

                    hookHead.teleport(currentHookLoc);
                    leashHolder.teleport(currentHookLoc.clone().subtract(0, 0.5, 0));

                    if (hookedTarget != null && !hookedTarget.isDead()) {
                        hookedTarget.teleport(currentHookLoc.clone().add(0, 0.2, 0));
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
                        if (entity instanceof Item item && item.getItemStack().getType() == Material.LEAD) {
                            item.remove();
                        }
                    }
                }
                if (hookHead != null && hookHead.isValid()) {
                    hookHead.remove();
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);

        return true;
    }
}