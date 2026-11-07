package com.dotaCraft.Hero.impl.invoker;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Hero.impl.pudge.FleshHeap;
import com.dotaCraft.Manager.IconManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.Queue;

public class Invoker extends Hero {

    private final Queue<Ability> activeOrbs = new LinkedList<>();
    private double currentExortBonusDamage = 0;

    public Invoker(Player player) {
        super(player, "Invoker", Attribute.INTELLECT, 19, 14, 22, 2.15, 1.1, 2.5, 2.0, 4.0, 1.7, AttackType.RANGED, 600.0, 900.0, 20, 26, 280);

        this.addAbility(0, new Quas());
        this.addAbility(1, new Wex());
        this.addAbility(2, new Exort());
        this.addAbility(3, new Invoke());

    }

    @Override
    public void launchProjectile(Hero attacker, Entity target, double damage) {
        Player player = attacker.getPlayer();

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

                if (direction.length() <= blocksPerTick) {
                    onProjectileHit(target, damage);
                    cancel();
                    return;
                }

                direction.normalize().multiply(blocksPerTick);
                currentLoc.add(direction);

                if (currentLoc.getWorld() != null) {
                    currentLoc.getWorld().spawnParticle(Particle.FLAME, currentLoc, 3, 0.03, 0.03, 0.03, 0.01);
                    currentLoc.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, currentLoc, 1, 0.03, 0.03, 0.03, 0.01);
                    currentLoc.getWorld().spawnParticle(Particle.FIREWORK, currentLoc, 1, 0.01, 0.01, 0.01, 0.01);
                }
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 1L);
    }

    public void addOrb(Ability orb) {
        if (activeOrbs.size() >= 3) {
            activeOrbs.poll();
        }

        activeOrbs.add(orb);

        recalculateOrbStats();
    }

    private void recalculateOrbStats() {
        Player player = getPlayer();

        Exort exortAbility = null;
        for (int i = 0; i < 6; i++) {
            if (getAbilityInSlot(i) instanceof Exort exort) {
                exortAbility = exort;
                break;
            }
        }

        if (currentExortBonusDamage > 0) {
            setMainDamage(-currentExortBonusDamage);
            currentExortBonusDamage = 0;
        }

        if (exortAbility != null) {
            double singleOrbBonus = exortAbility.getCurrentHandDamageBonus();
            int exortCount = 0;

            for (Ability orb : activeOrbs) {
                if (orb instanceof Exort) {
                    exortCount++;
                }
            }

            currentExortBonusDamage = singleOrbBonus * exortCount;

            if (currentExortBonusDamage > 0) {
                setMainDamage(currentExortBonusDamage);
            }
        }

        if (player != null) {
            IconManager.clearOrbIcons(player);
            for (Ability orb : activeOrbs) {
                if (orb instanceof Quas) {
                    IconManager.spawnPositiveIcon(player, "\uE401");
                } else if (orb instanceof Wex) {
                    IconManager.spawnPositiveIcon(player, "\uE402");
                } else if (orb instanceof Exort) {
                    IconManager.spawnPositiveIcon(player, "\uE403");
                }
            }
        }
    }

    public void invokeSpell(Ability newSpell) {
        Ability currentSlot3 = getAbilityInSlot(3);

        if (currentSlot3 != null && currentSlot3.getClass().equals(newSpell.getClass())) {
            return;
        }

        // check slots:
        if (currentSlot3 != null) {
            addAbility(4, currentSlot3);
        }

        addAbility(3, newSpell);

        getPlayer().sendMessage("§a[Invoke] §fNew ability added : §e" + newSpell.getClass().getSimpleName());
    }

    public Queue<Ability> getActiveOrbs() {
        return activeOrbs;
    }
}
