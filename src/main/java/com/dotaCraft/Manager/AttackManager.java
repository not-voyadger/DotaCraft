package com.dotaCraft.Manager;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class AttackManager implements Listener {

    private final Map lastInputTime = new HashMap<>();
    private final Set activeAttackingPlayers = new HashSet<>();

    public AttackManager() {
        startAttackLoop();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        registerPlayerInput(player);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        // Гасим ванильный урон
        event.setCancelled(true);

        registerPlayerInput(player);
    }

    private void registerPlayerInput(Player player) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        lastInputTime.put(player.getUniqueId(), System.currentTimeMillis());
        activeAttackingPlayers.add(player.getUniqueId());

        tryExecuteAttack(hero);
    }

    private void startAttackLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator iterator = activeAttackingPlayers.iterator();

                while (iterator.hasNext()) {
                    UUID uuid = (UUID) iterator.next();
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null || !player.isOnline()) {
                        iterator.remove();
                        lastInputTime.remove(uuid);
                        continue;
                    }

                    Hero hero = HeroManager.getHero(player);
                    if (hero == null) {
                        iterator.remove();
                        lastInputTime.remove(uuid);
                        continue;
                    }

                    double range = hero.getAttackRange();
                    LivingEntity currentTarget = findPrimaryTarget(player, range);

                    if (currentTarget != null) {
                        lastInputTime.put(uuid, now);
                    }

                    Long lastInput = (Long) lastInputTime.get(uuid);

                    if (lastInput == null || (now - lastInput) > 350) {
                        player.sendMessage("§7[Debug Attack] Зажатие прекращено.");
                        iterator.remove();
                        lastInputTime.remove(uuid);
                        continue;
                    }

                    tryExecuteAttack(hero);
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 1L);
    }

    private void tryExecuteAttack(Hero hero) {
        Player player = hero.getPlayer();

        if (!hero.canAttack()) {
            return;
        }

        hero.resetAttackCooldown();

        player.sendMessage("§a[Debug Attack] §lУДАР ПРОШЁЛ! §f(AS: " + hero.getAttackSpeed() +
                ", BAT: " + hero.getBaseAttackTime() +
                ", Interval: " + hero.getAttackIntervalMillis() + "ms)");
        performAttack(hero);
    }

    private void performAttack(Hero attackerHero) {
        Player player = attackerHero.getPlayer();
        player.swingMainHand();

        double range = attackerHero.getAttackRange();
        LivingEntity primaryTarget = findPrimaryTarget(player, range);
        double damage = attackerHero.getMainDamage();

        if (attackerHero.isRanged()) {
            // --- RANGE ---
            if (primaryTarget != null) {
                launchProjectile(attackerHero, primaryTarget, damage);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CANDLE_BREAK,1.0f, 1.2f);
                player.sendMessage("§b[Debug Attack] Выстрел снаряда по: §6" + primaryTarget.getName());
            } else {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 0.8f, 1.2f);
                player.sendMessage("§7[Debug Attack] Выстрел по воздуху (нет цели)");
            }
        } else {
            // --- MELEE ---
            if (primaryTarget != null) {
                primaryTarget.setNoDamageTicks(0);
                primaryTarget.damage(damage, player);

                // Damage number
                ArmorStand hologram = primaryTarget.getWorld().spawn(primaryTarget.getLocation().add(0, 0.5, 0), ArmorStand.class, armorStand -> {
                    armorStand.setVisible(false);
                    armorStand.setGravity(false);
                    armorStand.setMarker(true);
                    armorStand.setInvulnerable(true);
                    armorStand.setCustomName("§f§l " + (int) damage);
                    armorStand.setCustomNameVisible(true);
                });

                Bukkit.getScheduler().runTaskLater(DotaCraft.getInstance(), hologram::remove, 10L);

                player.getWorld().playSound(primaryTarget.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.0f);
                player.sendMessage("§e[Debug Attack] §fПопадание по: §6" + primaryTarget.getName() + " §fУрон: §c" + damage);
            } else {
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 0.8f, 1.2f);
                player.sendMessage("§7[Debug Attack] Удар по воздуху (цель не найдена)");
            }

            // Cleave works only for close range!
            /*if (attackerHero.hasCleave()) {
                applyCleave(attackerHero, primaryTarget, damage * 0.7, 4.5, 120.0);
            }*/
        }
    }

    private LivingEntity findPrimaryTarget(Player player, double range) {
        Location eyeLoc = player.getEyeLocation();
        Vector dir = eyeLoc.getDirection();

        var rayTrace = player.getWorld().rayTraceEntities(
                eyeLoc,
                dir,
                range,
                0.25,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        if (rayTrace != null && rayTrace.getHitEntity() instanceof LivingEntity target) {
            return target;
        }

        LivingEntity bestTarget = null;
        double closestAngle = Math.toRadians(8);

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (!(entity instanceof LivingEntity target) || entity.equals(player)) {
                continue;
            }

            Location targetCenter = target.getLocation().add(0, target.getHeight() / 2.0, 0);
            Vector toTarget = targetCenter.subtract(eyeLoc).toVector();

            double distance = toTarget.length();
            if (distance > range) continue;

            double angle = dir.angle(toTarget);

            if (angle < closestAngle) {
                closestAngle = angle;
                bestTarget = target;
            }
        }

        return bestTarget;
    }

    private void launchProjectile(Hero attacker, LivingEntity target, double damage) {
        Player player = attacker.getPlayer();

        boolean isLowground = target.getLocation().getY() - player.getLocation().getY() > 0.5;

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
                double distance = direction.length();

                if (distance <= blocksPerTick) {

                    boolean isMiss = isLowground && ThreadLocalRandom.current().nextInt(100) < 25; // 25% шанс

                    if (isMiss) {
                        target.getWorld().playSound(targetLoc, Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0f, 1.5f);

                        // MISS
                        ArmorStand hologram = target.getWorld().spawn(target.getLocation().clone().add(0, 1, 0), ArmorStand.class, armorStand -> {
                            armorStand.setVisible(false);
                            armorStand.setGravity(false);
                            armorStand.setMarker(true);
                            armorStand.setInvulnerable(true);
                            armorStand.setCustomName("§c§lMISS");
                            armorStand.setCustomNameVisible(true);
                        });

                        Bukkit.getScheduler().runTaskLater(DotaCraft.getInstance(), hologram::remove, 12L);
                        player.sendMessage("§c[Debug Attack] §7Промах по " + target.getName() + " (Lowground)");

                    } else {
                        target.setNoDamageTicks(0);
                        target.damage(damage, player);

                        ArmorStand hologram = target.getWorld().spawn(target.getLocation().clone().add(0, 0.8, 0), ArmorStand.class, armorStand -> {
                            armorStand.setVisible(false);
                            armorStand.setGravity(false);
                            armorStand.setMarker(true);
                            armorStand.setInvulnerable(true);
                            armorStand.setCustomName("§f§l" + (int) damage);
                            armorStand.setCustomNameVisible(true);
                        });

                        target.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, targetLoc, 10, 0.2, 0.2, 0.2, 0.1);
                        target.getWorld().playSound(targetLoc, Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.2f);
                        player.sendMessage("§e[Debug Attack] §fСнаряд попал в: §6" + target.getName() + " §fУрон: §c" + damage);

                        Bukkit.getScheduler().runTaskLater(DotaCraft.getInstance(), hologram::remove, 12L);
                    }

                    cancel();
                    return;
                }

                direction.normalize().multiply(blocksPerTick);
                currentLoc.add(direction);

                currentLoc.getWorld().spawnParticle(Particle.ITEM_SNOWBALL, currentLoc, 2, 0.05, 0.05, 0.05, 0.01);
                currentLoc.getWorld().spawnParticle(Particle.FIREWORK, currentLoc, 1, 0, 0, 0, 0);
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 1L);
    }

    /*private void applyCleave(Hero attackerHero, LivingEntity primaryTarget, double cleaveDamage, double radius, double maxAngle) {
        Player attacker = attackerHero.getPlayer();
        Location center = attacker.getLocation();
        Vector direction = center.getDirection().setY(0).normalize();

        Collection nearby = attacker.getWorld().getNearbyEntities(center, radius, radius, radius);

        for (Entity entity : nearby) {
            if (!(entity instanceof LivingEntity target) || entity.equals(attacker) || entity.equals(primaryTarget)) {
                continue;
            }

            Vector toTarget = target.getLocation().toVector().subtract(center.toVector()).setY(0).normalize();
            double angle = Math.toDegrees(direction.angle(toTarget));

            if (angle <= maxAngle / 2.0) {
                target.setNoDamageTicks(0);
                target.damage(cleaveDamage, attacker);
                target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 1);
            }
        }
    }*/
}