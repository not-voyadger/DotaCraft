package com.dotaCraft.Manager;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class DamageManager {

    public static double dealDamage(Hero attacker, LivingEntity target, double rawDamage, Ability.DamageTypes damageType) {
        if (target == null || target.isDead()) return 0;

        Hero targetHero = (target instanceof Player playerTarget) ? HeroManager.getHero(playerTarget) : null;

        double finalDamage = rawDamage;
        boolean isCrit = false;

        if (damageType == Ability.DamageTypes.PHYSICAL && attacker != null) {
            double critChance = attacker.getCritChance();

            if (critChance > 0 && ThreadLocalRandom.current().nextDouble(100.0) < critChance) {
                double critMultiplier = attacker.getCritMultiplier();
                finalDamage *= critMultiplier;
                isCrit = true;
            }
        }

        if (damageType == Ability.DamageTypes.PHYSICAL) {
            double armor = targetHero != null ? targetHero.getArmor() : 0.0;
            double reduction = calculateArmorReduction(armor);
            finalDamage *= (1.0 - reduction);
        } else if (damageType == Ability.DamageTypes.MAGICAL) {
            double magicResist = targetHero != null ? targetHero.getMagicResist() : 0.25;
            finalDamage *= (1.0 - magicResist);
        } else if (damageType == Ability.DamageTypes.PURE) {
            // TO DO: pure damage isn't lowered by resists
        }

        // Passives
        if (targetHero != null) {
            for (int i = 0; i < 6; i++) {
                Ability ability = targetHero.getAbilityInSlot(i);
                if (ability != null && ability.getAbilityLevel() > 0) {
                    finalDamage = ability.onTakeDamage(targetHero, finalDamage);
                }
            }
        }

        if (finalDamage <= 0) return 0;

        if (targetHero != null) {
            targetHero.setCurrentHealth((int) Math.max(0, targetHero.getCurrentHealth() - finalDamage));
            targetHero.setLastAttacker(attacker);

            if (targetHero.getCurrentHealth() <= 0) {
                onHeroDeath(attacker, targetHero);
            }
        } else {
            target.setNoDamageTicks(0);
            target.damage(finalDamage, attacker != null ? attacker.getPlayer() : null);
        }

        // Damage indicator
        if (isCrit) {
            HologramManager.spawnCritIndicator(target, finalDamage);
        } else {
            HologramManager.spawnDamageIndicator(target, finalDamage, damageType);
        }

        return finalDamage;
    }

    private static double calculateArmorReduction(double armor) {
        if (armor >= 0) {
            return (0.06 * armor) / (1 + 0.06 * armor);
        } else {
            return -((0.06 * Math.abs(armor)) / (1 + 0.06 * Math.abs(armor)));
        }
    }

    private static void onHeroDeath(Hero killer, Hero victim) {
        if (killer != null) {
            killer.getPlayer().sendMessage("§a[Kills] You've killed " + victim.getHeroName() + "!");
        }
        victim.getPlayer().sendMessage("§c[Death] You died!");
    }
}