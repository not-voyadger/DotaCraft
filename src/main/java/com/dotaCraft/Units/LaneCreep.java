package com.dotaCraft.Units;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Objective.Objective;
import com.dotaCraft.Utils.DotaUnits;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class LaneCreep extends Creep {

    public enum CreepType {
        MELEE("radiant_melee", "Radiant Melee Creep", 550, 21, 2, 21, 41, 1.5, EntityType.COW, "melee_creep"),
        RANGED("radiant_ranged", "Radiant Ranged Creep", 300, 26, 0, 26, 41, 4.0, EntityType.SKELETON, null),
        SIEGE("radiant_siege", "Radiant Siege Creep", 850, 41, 0, 41, 73, 4.5, EntityType.IRON_GOLEM, null);

        final String id;
        final String name;
        final double hp;
        final double damage;
        final double armor;
        final int gold;
        final int xp;
        final double attackRange;
        final EntityType type;
        final String modelId;

        CreepType(String id, String name, double hp, double damage, double armor, int gold, int xp, double attackRange, EntityType type, String modelId) {
            this.id = id;
            this.name = name;
            this.hp = hp;
            this.damage = damage;
            this.armor = armor;
            this.gold = gold;
            this.xp = xp;
            this.attackRange = attackRange;
            this.type = type;
            this.modelId = modelId;
        }
    }

    private final CreepType creepType;
    private final List<Location> waypoints;
    private int currentWaypointIndex = 0;
    private LivingEntity currentTarget = null;
    private Objective currentObjectiveTarget = null;

    private long lastAttackTime = 0;
    private int tickCounter = 0;

    private LivingEntity forcedAggroTarget = null;
    private long aggroExpireTime = 0;
    private long aggroCooldownEndTime = 0;

    private ModeledEntity modeledEntity;
    private ActiveModel activeModel;

    public LaneCreep(CreepType creepType, String team, Location spawnLoc, List<Location> waypoints) {
        super(
                team.toLowerCase() + "_" + creepType.name().toLowerCase(),
                (team.equalsIgnoreCase("radiant") ? "Radiant " : "Dire ") + creepType.name,
                creepType.type,
                team.toLowerCase(),
                creepType.hp,
                creepType.damage,
                creepType.armor,
                creepType.gold,
                creepType.xp,
                DotaUnits.toBlocks(500.0), // Vision
                creepType.attackRange
        );
        this.creepType = creepType;
        this.waypoints = waypoints;
    }

    @Override
    public LivingEntity spawn(Location location) {
        LivingEntity spawnedEntity = super.spawn(location);

        if (spawnedEntity != null && creepType.modelId != null) {
            applyModelEngineModel(spawnedEntity, creepType.modelId);
        }

        return spawnedEntity;
    }

    private boolean hasModel = false;

    private void applyModelEngineModel(LivingEntity entity, String modelId) {
        if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {
            return;
        }

        entity.setSilent(true);
        entity.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false
        ));

        this.activeModel = com.dotaCraft.Utils.ModelEngineHook.applyModel(entity, modelId, 0.75f);
        this.hasModel = (this.activeModel != null);
    }

    @Override
    public void die(Hero killer) {
        if (hasModel && getEntity() != null) {
            if (org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ModelEngine")) {
                com.dotaCraft.Utils.ModelEngineHook.removeModel(getEntity(), creepType.modelId);
            }
        }
        super.die(killer);
    }

    @Override
    public void onTick() {
        if (isDead() || getEntity() == null) return;
        Mob mob = (Mob) getEntity();
        tickCounter++;
        long now = System.currentTimeMillis();

        if (forcedAggroTarget != null) {
            if (now > aggroExpireTime || !forcedAggroTarget.isValid() || forcedAggroTarget.isDead()
                    || mob.getLocation().distance(forcedAggroTarget.getLocation()) > getVisionRadius()) {
                forcedAggroTarget = null;
            }
        }

        boolean needTargetRecalc = (tickCounter % 10 == 0)
                || (currentTarget == null && currentObjectiveTarget == null)
                || (currentTarget != null && (!currentTarget.isValid() || currentTarget.isDead()))
                || (currentObjectiveTarget != null && !currentObjectiveTarget.isAlive());

        if (needTargetRecalc) {
            if (forcedAggroTarget != null) {
                currentTarget = forcedAggroTarget;
                currentObjectiveTarget = null;
            } else {
                updateTargetByPriority(mob);
            }
        }

        if (currentTarget != null) {
            handleEntityCombat(mob, currentTarget);
        } else if (currentObjectiveTarget != null) {
            handleObjectiveCombat(mob, currentObjectiveTarget);
        } else {
            followWaypoints(mob);
        }
    }

    private void handleEntityCombat(Mob mob, LivingEntity target) {
        double dist = mob.getLocation().distance(target.getLocation());

        if (dist > getVisionRadius() || !target.isValid() || target.isDead()) {
            currentTarget = null;
            mob.getPathfinder().stopPathfinding();
        } else if (dist <= getAttackRange()) {
            mob.getPathfinder().stopPathfinding();
            tryAttack(target);
        } else {
            mob.getPathfinder().moveTo(target.getLocation(), 1.0);
        }
    }

    private void handleObjectiveCombat(Mob mob, Objective obj) {
        Entity objEntity = obj.getEntity();
        if (!obj.isAlive() || objEntity == null || !objEntity.isValid()) {
            currentObjectiveTarget = null;
            return;
        }

        double dist = mob.getLocation().distance(objEntity.getLocation());
        if (dist <= getAttackRange() + 1.5) {
            mob.getPathfinder().stopPathfinding();
            tryAttackEntity(objEntity);
        } else {
            mob.getPathfinder().moveTo(objEntity.getLocation(), 1.0);
        }
    }

    private void followWaypoints(Mob mob) {
        if (waypoints == null || waypoints.isEmpty() || currentWaypointIndex >= waypoints.size()) return;

        Location targetLoc = waypoints.get(currentWaypointIndex);
        if (mob.getLocation().distance(targetLoc) < 2.5) {
            currentWaypointIndex++;
            if (currentWaypointIndex >= waypoints.size()) return;
            targetLoc = waypoints.get(currentWaypointIndex);
        }

        mob.getPathfinder().moveTo(targetLoc, 1.0);
    }

    private void updateTargetByPriority(Mob mob) {
        LivingEntity bestUnit = null;
        double closestUnitDistSq = getVisionRadius() * getVisionRadius();

        for (Entity nearby : mob.getWorld().getNearbyEntities(mob.getLocation(), getVisionRadius(), 4.0, getVisionRadius())) {
            if (!(nearby instanceof LivingEntity target)) continue;
            if (target.equals(mob) || mob.getPassengers().contains(target)) continue;
            if (target instanceof ArmorStand || target instanceof Display) continue;
            if (target.hasMetadata("no_vision_check")) continue;
            if (target.isDead() || !target.isValid()) continue;

            if (DotaCraft.getInstance().getObjectiveManager().getObjectiveByEntity(target) != null) {
                continue;
            }

            if (isAlly(target)) continue;

            double distSq = mob.getLocation().distanceSquared(target.getLocation());
            if (distSq < closestUnitDistSq) {
                closestUnitDistSq = distSq;
                bestUnit = target;
            }
        }

        Objective nearestTower = DotaCraft.getInstance().getObjectiveManager()
                .getNearestEnemyObjective(mob.getLocation(), this.getTeam(), getVisionRadius());

        if (this.creepType == CreepType.SIEGE && nearestTower != null) {
            this.currentObjectiveTarget = nearestTower;
            this.currentTarget = null;
            return;
        }

        if (bestUnit != null) {
            this.currentTarget = bestUnit;
            this.currentObjectiveTarget = null;
        } else if (nearestTower != null) {
            this.currentObjectiveTarget = nearestTower;
            this.currentTarget = null;
        } else {
            this.currentTarget = null;
            this.currentObjectiveTarget = null;
        }
    }

    public void onEnemyHeroAttackAlly(LivingEntity enemyHero) {
        long now = System.currentTimeMillis();
        if (now < aggroCooldownEndTime) return;
        if (getEntity() == null || enemyHero == null) return;

        if (getEntity().getLocation().distance(enemyHero.getLocation()) <= getVisionRadius()) {
            this.forcedAggroTarget = enemyHero;
            this.currentTarget = enemyHero;
            this.currentObjectiveTarget = null;

            this.aggroExpireTime = now + 2300;
            this.aggroCooldownEndTime = now + 3000;
        }
    }

    private LivingEntity findTarget() {
        if (getEntity() == null || !getEntity().isValid()) return null;

        LivingEntity best = null;
        double closestDistSq = getVisionRadius() * getVisionRadius();

        for (Entity nearby : getEntity().getWorld().getNearbyEntities(getEntity().getLocation(), getVisionRadius(), 4.0, getVisionRadius())) {
            if (!(nearby instanceof LivingEntity target)) continue;

            if (target.equals(getEntity())) continue;
            if (getEntity().getPassengers().contains(target)) continue;

            if (target instanceof org.bukkit.entity.ArmorStand ||
                    target instanceof org.bukkit.entity.Display) continue;
            if (target.hasMetadata("no_vision_check")) continue;

            if (isAlly(target)) continue;

            double distSq = getEntity().getLocation().distanceSquared(target.getLocation());
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                best = target;
            }
        }
        return best;
    }

    private boolean isAlly(LivingEntity target) {
        if (target == null) return true;

        if (target instanceof Player player) {
            Hero hero = HeroManager.getHero(player);
            if (hero == null || hero.getTeam() == null) return true;
            return hero.getTeam().equalsIgnoreCase(this.getTeam());
        }

        LaneCreep activeCreep = DotaCraft.getInstance().getCreepLaneManager().getActiveLaneCreep(target.getUniqueId());
        if (activeCreep != null) {
            if (activeCreep.getTeam() == null) return true;
            return activeCreep.getTeam().equalsIgnoreCase(this.getTeam());
        }

        Objective obj = DotaCraft.getInstance().getObjectiveManager().getObjectiveByEntity(target);
        if (obj != null) {
            return obj.getTeam().equalsIgnoreCase(this.getTeam());
        }

        NeutralCreep neutral = DotaCraft.getInstance().getNeutralManager().getActiveCreep(target.getUniqueId());
        if (neutral != null) {
            return true;
        }

        return true;
    }

    private void tryAttack(LivingEntity target) {
        long now = System.currentTimeMillis();
        if (now - lastAttackTime >= 1000) {
            lastAttackTime = now;
            DamageManager.dealDamage(null, target, getDamage(), com.dotaCraft.Ability.Ability.DamageTypes.PHYSICAL);

            if (activeModel != null && activeModel.getAnimationHandler() != null) {
                activeModel.getAnimationHandler().playAnimation("melee_attack", 0.1, 0.1, 1.0, true);
            }

            getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.2f);
        }
    }

    private void tryAttackEntity(Entity target) {
        long now = System.currentTimeMillis();
        if (now - lastAttackTime >= 1000) {
            lastAttackTime = now;
            DamageManager.dealDamage(null, target, getDamage(), com.dotaCraft.Ability.Ability.DamageTypes.PHYSICAL);

            if (activeModel != null && activeModel.getAnimationHandler() != null) {
                activeModel.getAnimationHandler().playAnimation("melee_attack", 0.1, 0.1, 1.0, true);
            }

            getEntity().getWorld().playSound(getEntity().getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.6f, 1.2f);
        }
    }

    @Override
    protected void updateNameTag() {
        if (getEntity() == null) return;
        DotaCraft.getInstance().getHealthBarManager().updateOrCreateBar(
                getEntity(),
                getEntity().getLocation().add(0, getEntity().getHeight(), 0),
                getName(),
                getCurrentHealth(),
                getMaxHealth(),
                1.0f,
                null,
                getTeam()
        );
    }
}