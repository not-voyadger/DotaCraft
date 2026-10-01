package com.dotaCraft.Manager;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

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

                    // Ищем, смотрит ли игрок прямо сейчас на моба
                    LivingEntity currentTarget = findPrimaryTarget(player, 3.5);

                    // Если прицел наведён на моба, сбрасываем таймаут зажатия,
                    // так как клиент заглушает отправку пакетов клика при наведении на Entity!
                    if (currentTarget != null) {
                        lastInputTime.put(uuid, now);
                    }

                    Long lastInput = (Long) lastInputTime.get(uuid);

                    // Если прицел не на мобе И с последнего клика по воздуху/блоку прошло > 350мс
                    if (lastInput == null || (now - lastInput) > 350) {
                        player.sendMessage("§7[Debug Attack] Зажатие прекращено.");
                        iterator.remove();
                        lastInputTime.remove(uuid);
                        continue;
                    }

                    // Выполняем удар по кулдауну
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

        double attackRange = 3.5;
        LivingEntity primaryTarget = findPrimaryTarget(player, attackRange);

        double damage = attackerHero.getMainDamage();

        if (primaryTarget != null) {
            primaryTarget.setNoDamageTicks(0);
            primaryTarget.damage(damage, player);

            player.getWorld().playSound(primaryTarget.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.0f);
            player.sendMessage("§e[Debug Attack] §fПопадание по: §6" + primaryTarget.getName() + " §fУрон: §c" + damage);
        } else {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, 0.8f, 1.2f);
            player.sendMessage("§7[Debug Attack] Удар по воздуху (цель не найдена)");
        }

        /*if (attackerHero.hasCleave()) {
            applyCleave(attackerHero, primaryTarget, damage * 0.7, 4.5, 120.0);
        }*/
    }

    private LivingEntity findPrimaryTarget(Player player, double range) {
        var rayTrace = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.8,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        if (rayTrace != null && rayTrace.getHitEntity() instanceof LivingEntity target) {
            return target;
        }

        for (Entity entity : player.getNearbyEntities(range, range, range)) {
            if (entity instanceof LivingEntity target && !entity.equals(player)) {
                Vector toTarget = target.getLocation().add(0, 1, 0).subtract(player.getEyeLocation()).toVector();
                if (player.getEyeLocation().getDirection().angle(toTarget) < Math.toRadians(35)) {
                    return target;
                }
            }
        }

        return null;
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