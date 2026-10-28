package com.dotaCraft.Units;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Utils.DotaUnits;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class LaneCreep extends Creep {

    public enum CreepType {
        MELEE("radiant_melee", "Radiant Melee Creep", 550, 21, 2, 21, 41, 1.5, EntityType.VILLAGER, "melee_creep"),
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
    private long lastAttackTime = 0;

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

        this.activeModel = com.dotaCraft.Utils.ModelEngineHook.applyModel(entity, modelId, 0.7  5f);
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

        if (currentTarget == null || !currentTarget.isValid() || currentTarget.isDead()) {
            currentTarget = findTarget();
        }

        if (currentTarget != null) {
            double dist = mob.getLocation().distance(currentTarget.getLocation());

            if (dist <= getAttackRange()) {
                mob.getPathfinder().stopPathfinding();
                tryAttack(currentTarget);
            } else {
                mob.getPathfinder().moveTo(currentTarget.getLocation(), 1.25);
            }
        } else {
            followWaypoints(mob);
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

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double closestDist = getVisionRadius();

        for (Entity nearby : getEntity().getWorld().getNearbyEntities(getEntity().getLocation(), closestDist, 4.0, closestDist)) {
            if (!(nearby instanceof LivingEntity target) || nearby.equals(getEntity()) || nearby.hasMetadata("no_vision_check")) continue;

            if (target instanceof Player player) {
                Hero hero = HeroManager.getHero(player);
                if (hero != null && hero.getTeam().equalsIgnoreCase(getTeam())) continue;
            }

            double dist = getEntity().getLocation().distance(target.getLocation());
            if (dist < closestDist) {
                closestDist = dist;
                best = target;
            }
        }
        return best;
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