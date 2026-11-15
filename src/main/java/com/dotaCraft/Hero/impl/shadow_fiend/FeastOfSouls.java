package com.dotaCraft.Hero.impl.shadow_fiend;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.IconManager;
import com.dotaCraft.Units.LaneCreep;
import com.dotaCraft.Units.NeutralCreep;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FeastOfSouls extends Ability {

    private final double[] additionalAttackSpeed = {35, 50, 65, 80};
    private final double[] additionalMoveSpeedInPercent = {4, 6, 8, 10};
    private final int[] maxVictimCount = {4, 6, 8, 10};
    private final int soulsFromHero = 3;
    private final int soulsFromCreep = 1;

    private final double searchRadiusBlocks = DotaUnits.toBlocks(600);
    private final int maxTargetsPerTick = 2;
    private final int collectIntervalTicks = 10;

    public static final String ICON = ""; // TODO
    public static final String EFFECT_ICON = "\uE507";

    private BukkitTask activeBuffTask = null;
    private BukkitTask activeOvercapTimerTask = null;

    public FeastOfSouls() {
        super(
                DamageTypes.NONE,
                AbilityTypes.NO_TARGET,
                TargetTypes.NONE,
                DispelTypes.BASIC,
                new double[] {0, 0, 0, 0},
                new double[] {60, 65, 70, 75},
                new double[] {0, 0, 0, 0},
                new double[] {21, 21, 21, 21},
                new double[] {0, 0, 0, 0},
                0,
                new double[] {0, 0, 0, 0},
                new double[] {8, 8, 8, 8},
                1,
                4,
                0,
                false,
                false,
                false,
                ICON
        );
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();
        if (player == null || !player.isOnline()) return false;

        if (activeBuffTask != null && !activeBuffTask.isCancelled()) {
            activeBuffTask.cancel();
        }
        if (activeOvercapTimerTask != null && !activeOvercapTimerTask.isCancelled()) {
            activeOvercapTimerTask.cancel();
        }

        int levelIndex = Math.max(0, Math.min(getAbilityLevel() - 1, 3));

        double bonusAS = additionalAttackSpeed[levelIndex];
        double bonusMSPercent = additionalMoveSpeedInPercent[levelIndex] / 100.0;
        int maxVictims = maxVictimCount[levelIndex];
        int durationTicks = (int) (getDuration(getAbilityLevel()) * 20); // 160 тиков (8 сек)

        IconManager.spawnPositiveIcon(player, EFFECT_ICON, durationTicks);
        // TODO: add sound here.

        hero.addBonusAttackSpeed(bonusAS);
        hero.addBonusMoveSpeedPercent(bonusMSPercent);

        Map<UUID, Integer> drainedTargets = new HashMap<>();

        activeBuffTask = new BukkitRunnable() {
            private int currentTick = 0;
            private boolean statsRemoved = false;

            private void removeBonusStats() {
                if (!statsRemoved) {
                    hero.addBonusAttackSpeed(-bonusAS);
                    hero.addBonusMoveSpeedPercent(-bonusMSPercent);
                    IconManager.removePositiveIcon(player, EFFECT_ICON);
                    statsRemoved = true;
                }
            }

            @Override
            public void run() {
                if (!player.isOnline() || player.isDead() || hero.isHeroDead()) {
                    removeBonusStats();
                    Necromastery necromastery = getNecromastery(hero);
                    if (necromastery != null) {
                        necromastery.clampToMaxSouls(hero);
                    }
                    cancel();
                    return;
                }

                if (currentTick >= durationTicks) {
                    removeBonusStats();
                    handleBuffExpiration(hero, drainedTargets);
                    cancel();
                    return;
                }

                // Каждые 0.5 сек (10 тиков) собираем души с 2 врагов
                if (currentTick % collectIntervalTicks == 0 && drainedTargets.size() < maxVictims) {
                    collectSoulsTick(hero, player, drainedTargets, maxVictims);
                }

                currentTick++;
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);

        return true;
    }

    private void collectSoulsTick(Hero caster, Player player, Map<UUID, Integer> drainedTargets, int maxVictims) {
        Necromastery necromastery = getNecromastery(caster);
        if (necromastery == null) return;

        int collectedThisTick = 0;
        List<Entity> nearbyEntities = player.getNearbyEntities(searchRadiusBlocks, searchRadiusBlocks, searchRadiusBlocks);

        for (Entity entity : nearbyEntities) {
            if (collectedThisTick >= maxTargetsPerTick || drainedTargets.size() >= maxVictims) {
                break;
            }

            if (!(entity instanceof LivingEntity target) || target.equals(player)) {
                continue;
            }

            if (target instanceof ArmorStand || target instanceof Display || target.hasMetadata("no_vision_check")) {
                continue;
            }

            UUID targetId = target.getUniqueId();

            if (drainedTargets.containsKey(targetId)) {
                continue;
            }

            if (!isTargetAliveAndValidEnemy(caster, target)) {
                continue;
            }

            int soulsToAdd = isEnemyHero(target) ? soulsFromHero : soulsFromCreep;

            drainedTargets.put(targetId, soulsToAdd);
            necromastery.addTemporarySouls(caster, soulsToAdd);
            spawnSoulDrainEffect(target, player);

            collectedThisTick++;
        }
    }

    private void handleBuffExpiration(Hero caster, Map<UUID, Integer> drainedTargets) {
        Necromastery necromastery = getNecromastery(caster);
        if (necromastery == null) return;

        int soulsToReturnToSurvivors = 0;
        int soulsFromDeadEnemies = 0;

        for (Map.Entry<UUID, Integer> entry : drainedTargets.entrySet()) {
            UUID targetId = entry.getKey();
            int souls = entry.getValue();

            if (isTrackedEnemyAlive(targetId)) {
                soulsToReturnToSurvivors += souls;
            } else {
                soulsFromDeadEnemies += souls;
            }
        }

        if (soulsToReturnToSurvivors > 0) {
            necromastery.removeSouls(caster, soulsToReturnToSurvivors);
        }

        if (soulsFromDeadEnemies > 0) {
            activeOvercapTimerTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (caster.getPlayer() != null && caster.getPlayer().isOnline() && !caster.isHeroDead()) {
                        necromastery.clampToMaxSouls(caster);
                    }
                }
            }.runTaskLater(DotaCraft.getInstance(), 160L);
        } else {
            necromastery.clampToMaxSouls(caster);
        }
    }

    private Necromastery getNecromastery(Hero hero) {
        if (hero.getInnateAbility() instanceof Necromastery necromastery) {
            return necromastery;
        }
        return null;
    }

    private boolean isEnemyHero(LivingEntity target) {
        return target instanceof Player p && HeroManager.getHero(p) != null;
    }

    private boolean isTargetAliveAndValidEnemy(Hero caster, LivingEntity target) {
        if (target.isDead() || !target.isValid()) return false;

        if (target instanceof Player targetPlayer) {
            Hero targetHero = HeroManager.getHero(targetPlayer);
            if (targetHero == null || targetHero.isHeroDead() || targetHero.getCurrentHealth() <= 0) {
                return false;
            }
            return targetHero.getTeam() != null && !targetHero.getTeam().equalsIgnoreCase(caster.getTeam());
        }

        LaneCreep laneCreep = DotaCraft.getInstance().getCreepLaneManager().getActiveLaneCreep(target.getUniqueId());
        if (laneCreep != null) {
            return !laneCreep.isDead() && laneCreep.getTeam() != null && !laneCreep.getTeam().equalsIgnoreCase(caster.getTeam());
        }

        NeutralCreep neutralCreep = DotaCraft.getInstance().getNeutralManager().getActiveCreep(target.getUniqueId());
        if (neutralCreep != null) {
            return !neutralCreep.isDead();
        }

        return false;
    }

    private boolean isTrackedEnemyAlive(UUID targetId) {
        Player targetPlayer = Bukkit.getPlayer(targetId);
        if (targetPlayer != null) {
            Hero targetHero = HeroManager.getHero(targetPlayer);
            if (targetHero != null) {
                return targetPlayer.isOnline() && !targetPlayer.isDead() && !targetHero.isHeroDead() && targetHero.getCurrentHealth() > 0;
            }
        }

        LaneCreep laneCreep = DotaCraft.getInstance().getCreepLaneManager().getActiveLaneCreep(targetId);
        if (laneCreep != null) {
            return !laneCreep.isDead();
        }

        NeutralCreep neutralCreep = DotaCraft.getInstance().getNeutralManager().getActiveCreep(targetId);
        if (neutralCreep != null) {
            return !neutralCreep.isDead();
        }

        Entity entity = Bukkit.getEntity(targetId);
        if (entity instanceof LivingEntity living) {
            return living.isValid() && !living.isDead() && living.getHealth() > 0;
        }

        return false;
    }

    private void spawnSoulDrainEffect(LivingEntity from, Player to) {
        Location start = from.getLocation().add(0, from.getHeight() / 2.0, 0);
        Location end = to.getLocation().add(0, 1.1, 0);
        Vector diff = end.toVector().subtract(start.toVector());
        double dist = diff.length();
        if (dist <= 0.1) return;

        Vector step = diff.normalize().multiply(0.4);
        Location current = start.clone();

        for (double d = 0; d < dist; d += 0.4) {
            current.add(step);
            from.getWorld().spawnParticle(Particle.SOUL, current, 1, 0.05, 0.05, 0.05, 0.01);
        }
    }
}