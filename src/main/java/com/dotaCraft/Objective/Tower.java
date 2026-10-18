package com.dotaCraft.Objective;

import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.TreeManager;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Tower extends Objective {

    private final int tier;
    private final double attackDamage;
    private final double attackRange;
    private final double attackIntervalSeconds;
    private final double projectileSpeed;

    private long lastAttackTime = 0;
    private LivingEntity currentTarget = null;
    private ArmorStand nameHologram = null;

    private static class CutBlockInfo {
        final Location location;
        final BlockData originalData;

        CutBlockInfo(Location location, BlockData originalData) {
            this.location = location;
            this.originalData = originalData;
        }
    }

    public Tower(String name, TeamEnum team, int tier, Location location) {
        super(
                name,
                team,
                150 + (tier * 50),
                200 + (tier * 100),
                1000 + (tier * 400),
                tier > 1 ? 3.0 : 0.0,
                12.0 + (tier * 3.0),
                location,
                DotaUnits.toBlocks(1900.0)
        );

        this.tier = tier;
        this.attackDamage = 90.0 + (tier * 30.0);
        this.attackRange = DotaUnits.toBlocks(700.0);
        this.attackIntervalSeconds = 1.0;
        this.projectileSpeed = DotaUnits.toBlocks(1200.0);
    }

    @Override
    public void spawn(Location loc) {
        if (loc.getWorld() == null) return;

        Location spawnLoc = loc.clone();
        spawnLoc.setX(Math.floor(spawnLoc.getX()) + 0.5);
        spawnLoc.setZ(Math.floor(spawnLoc.getZ()) + 0.5);

        Interaction interaction = spawnLoc.getWorld().spawn(spawnLoc, Interaction.class, entity -> {
            entity.setInteractionWidth(2.5f);
            entity.setInteractionHeight(4.5f);
            entity.setResponsive(true);
        });

        setEntity(interaction);
        updateNameTag();
    }

    @Override
    public void onTick() {
        super.onTick();
        if (!isAlive() || getEntity() == null || !getEntity().isValid()) return;

        long now = System.currentTimeMillis();
        if (now - lastAttackTime >= (long) (attackIntervalSeconds * 1000)) {
            findTargetAndAttack();
        }
    }

    private void findTargetAndAttack() {
        Entity entity = getEntity();
        if (entity == null) return;

        Location eyeLoc = entity.getLocation().add(0, 3.0, 0);

        LivingEntity bestTarget = null;
        double closestDist = attackRange;

        for (Entity e : eyeLoc.getWorld().getNearbyEntities(eyeLoc, attackRange, attackRange, attackRange)) {
            if (!(e instanceof LivingEntity target)
                    || target instanceof ArmorStand
                    || e.equals(entity)
                    || e.equals(nameHologram)
                    || target.hasMetadata("no_vision_check")) continue;

            if (target instanceof Player player) {
                Hero hero = HeroManager.getHero(player);
                if (hero == null || hero.getTeam().equalsIgnoreCase(getTeamName())) continue;
            }

            double dist = eyeLoc.distance(target.getLocation());
            if (dist <= attackRange && dist < closestDist) {
                closestDist = dist;
                bestTarget = target;
            }
        }

        if (bestTarget != null) {
            this.currentTarget = bestTarget;
            this.lastAttackTime = System.currentTimeMillis();
            launchTowerProjectile(eyeLoc, currentTarget);
        }
    }

    private void launchTowerProjectile(Location spawnLoc, LivingEntity target) {
        spawnLoc.getWorld().playSound(spawnLoc, Sound.ENTITY_BLAZE_SHOOT, 0.3f, 0.6f);

        new BukkitRunnable() {
            private final Location currentLoc = spawnLoc.clone();
            private final double blocksPerTick = projectileSpeed / 20.0;
            private int maxTicks = 40;

            @Override
            public void run() {
                maxTicks--;

                if (target == null || !target.isValid() || target.isDead() || maxTicks <= 0) {
                    cancel();
                    return;
                }

                Location targetLoc = target.getLocation().add(0, target.getHeight() / 2.0, 0);
                Vector dir = targetLoc.toVector().subtract(currentLoc.toVector());

                if (dir.length() <= blocksPerTick) {
                    DamageManager.dealDamage(null, target, attackDamage, com.dotaCraft.Ability.Ability.DamageTypes.PHYSICAL);

                    targetLoc.getWorld().spawnParticle(Particle.EXPLOSION, targetLoc, 1);
                    //targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.2f);
                    cancel();
                    return;
                }

                dir.normalize().multiply(blocksPerTick);
                currentLoc.add(dir);

                Particle.DustOptions dust = getTeamEnum() == TeamEnum.RADIANT
                        ? new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 230, 100), 1.2f)
                        : new Particle.DustOptions(org.bukkit.Color.fromRGB(255, 60, 0), 1.2f);

                currentLoc.getWorld().spawnParticle(Particle.DUST, currentLoc, 4, 0.05, 0.05, 0.05, 0.0, dust);
            }
        }.runTaskTimer(DotaCraft.getInstance(), 1L, 1L);
    }

    @Override
    protected void updateNameTag() {
        Location barLoc = getLocation().clone().add(0, 7, 0);

        DotaCraft.getInstance().getHealthBarManager().updateOrCreateBar(
                getEntity(),
                barLoc,
                getName(),
                getCurrentHealth(),
                getMaxHealth(),
                3f
        );
    }

    @Override
    protected void onDeath(Hero killer, boolean isDenied) {
        DotaCraft.getInstance().getHealthBarManager().removeBar(getEntity());

        Block baseBlock = findTowerBaseBlock(getLocation());
        if (baseBlock != null) {
            destroyTower(baseBlock);
        }

        destroyTower(baseBlock);

        super.onDeath(killer, isDenied);
    }

    public boolean isTowerBlock(Block block) {
        if (block == null) return false;
        Material type = block.getType();

        boolean isGlass = type.name().endsWith("GLASS_PANE") || type.name().endsWith("GLASS");

        boolean isCalcite = type == Material.CALCITE;
        boolean isGoldBlock = type == Material.GOLD_BLOCK;
        boolean isSlabBlock = type == Material.POLISHED_DIORITE_SLAB;

        boolean isWall = Tag.WALLS.isTagged(type) || Tag.FENCES.isTagged(type);

        return isWall || isCalcite || isGoldBlock || isGlass || isSlabBlock;
    }

    public Block findTowerBaseBlock(Location towerLocation) {
        if (towerLocation == null || towerLocation.getWorld() == null) return null;

        Block centerBlock = towerLocation.getBlock();

        for (int y = 0; y <= 5; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Block candidate = centerBlock.getRelative(x, y, z);
                    if (isTowerBlock(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    public boolean destroyTower(Block startBlock) {
        if (!isTowerBlock(startBlock)) return false;

        List<CutBlockInfo> cutBlocks = new ArrayList<>();
        Queue<Block> queue = new LinkedList<>();
        Set<Location> visited = new HashSet<>();

        queue.add(startBlock);
        visited.add(startBlock.getLocation());

        while (!queue.isEmpty()) {
            Block current = queue.poll();
            cutBlocks.add(new CutBlockInfo(current.getLocation(), current.getBlockData()));

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block neighbor = current.getRelative(x, y, z);
                        Location neighborLoc = neighbor.getLocation();

                        if (!visited.contains(neighborLoc) && isTowerBlock(neighbor)) {
                            visited.add(neighborLoc);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        if (cutBlocks.isEmpty()) return false;

        // effects
        Location centerLoc = startBlock.getLocation().add(0.5, 0.5, 0.5);
        centerLoc.getWorld().playSound(centerLoc, Sound.BLOCK_WOOD_BREAK, 1.0f, 0.8f);
        centerLoc.getWorld().playSound(centerLoc, Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f);

        // tower becomes air
        for (CutBlockInfo info : cutBlocks) {
            info.location.getBlock().setType(Material.AIR, false);
        }
        return true;
    }

    public int getTier() { return tier; }

    public ArmorStand getNameHologram() {
        return nameHologram;
    }
}