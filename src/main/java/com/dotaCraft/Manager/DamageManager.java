package com.dotaCraft.Manager;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Units.NeutralCreep;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class DamageManager {

    public static double dealDamage(Hero attacker, LivingEntity target, double rawDamage, Ability.DamageTypes damageType) {
        if (target == null || target.isDead()) return 0;

        Hero targetHero = (target instanceof Player playerTarget) ? HeroManager.getHero(playerTarget) : null;

        NeutralCreep neutralCreep = DotaCraft.getInstance().getNeutralManager().getActiveCreep(target.getUniqueId());

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
            // Pure damage
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
        } else if (neutralCreep != null) {
            neutralCreep.takeDamage(finalDamage, attacker);

            if (neutralCreep.isDead()) {
                DotaCraft.getInstance().getNeutralManager().removeActiveCreep(target.getUniqueId());
            }
        } else {
            target.setNoDamageTicks(0);
            target.damage(finalDamage, attacker != null ? attacker.getPlayer() : null);
        }

        if (isCrit) {
            HologramManager.spawnCritIndicator(target, finalDamage);
        } else {
            HologramManager.spawnDamageIndicator(target, finalDamage, damageType);
        }

        return finalDamage;
    }

    public static double dealDamageFromAbility(Hero attacker, LivingEntity target, double rawDamage, Ability.DamageTypes damageType) {
        if (target == null || target.isDead()) return 0;

        double finalDamage = rawDamage;

        // if (attacker != null) {
        //     finalDamage *= (1.0 + attacker.getSpellAmp());
        // }

        return processAndApplyDamage(attacker, target, finalDamage, damageType, false);
    }

    public static double dealDamageFromAbility(Hero attacker, LivingEntity target, double rawDamage, Ability ability) {
        return dealDamageFromAbility(attacker, target, rawDamage, ability.getDamageType());
    }

    private static double processAndApplyDamage(Hero attacker, LivingEntity target, double rawDamage, Ability.DamageTypes damageType, boolean isCrit) {
        Hero targetHero = (target instanceof Player playerTarget) ? HeroManager.getHero(playerTarget) : null;
        NeutralCreep neutralCreep = DotaCraft.getInstance().getNeutralManager().getActiveCreep(target.getUniqueId());

        double finalDamage = rawDamage;

        if (damageType == Ability.DamageTypes.PHYSICAL) {
            double armor = targetHero != null ? targetHero.getArmor() : 0.0;
            double reduction = calculateArmorReduction(armor);
            finalDamage *= (1.0 - reduction);

        } else if (damageType == Ability.DamageTypes.MAGICAL) {
            double magicResist = targetHero != null ? targetHero.getMagicResist() : 0.25;
            finalDamage *= (1.0 - magicResist);

        } else if (damageType == Ability.DamageTypes.PURE) {
            // Pure is not reduced.
        }

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
        } else if (neutralCreep != null) {
            neutralCreep.takeDamage(finalDamage, attacker);

            if (neutralCreep.isDead()) {
                DotaCraft.getInstance().getNeutralManager().removeActiveCreep(target.getUniqueId());
            }
        } else {
            target.setNoDamageTicks(0);
            double newHealth = Math.max(0, target.getHealth() - finalDamage);
            target.setHealth(newHealth);
        }

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