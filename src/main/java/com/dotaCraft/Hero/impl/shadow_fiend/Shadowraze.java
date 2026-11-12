package com.dotaCraft.Hero.impl.shadow_fiend;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.ModifierManager;
import com.dotaCraft.Utils.DotaUnits;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public abstract class Shadowraze extends Ability {

    private static final double[] STACK_BONUS_DAMAGE = {50, 60, 70, 80};
    private static final double STACK_DURATION_SECONDS = 8.0;
    private static final String META_STACKS = "sf_raze_stacks";
    private static final String META_EXPIRE = "sf_raze_expire";

    private final double distanceBlocks;

    public Shadowraze(double distanceDotaUnits, String icon) {
        super(
                DamageTypes.MAGICAL,
                AbilityTypes.NO_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.BASIC,
                new double[]{90, 160, 230, 300},     // base damage
                new double[]{75, 80, 85, 90},        // manaCost
                new double[]{0, 0, 0, 0},            // healthCost
                new double[]{10, 10, 10, 10},        // coolDown
                new double[]{distanceDotaUnits},     // castRange
                0.55,                                // castPoint
                new double[]{250, 250, 250, 250},    // effectRadius (250 units)
                new double[]{STACK_DURATION_SECONDS},// duration
                1, 4, 1,
                false, false, false,
                icon
        );
        this.distanceBlocks = DotaUnits.toBlocks(distanceDotaUnits);
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();
        if (player == null || !player.isOnline()) return false;

        int level = getAbilityLevel();
        double radius = DotaUnits.toBlocks(getEffectRadius(level));
        double baseDamage = getDamage(level);
        double bonusPerStack = STACK_BONUS_DAMAGE[Math.min(level - 1, STACK_BONUS_DAMAGE.length - 1)];

        Vector forward = player.getLocation().getDirection().setY(0).normalize();
        Location razeCenter = player.getLocation().add(forward.multiply(distanceBlocks));

        World world = razeCenter.getWorld();
        if (world == null) return false;

        world.playSound(razeCenter, "dotacraft:shadow_fiend.shadow_fiend_shadowraze", 0.5f, 1.0f);
        drawRazeEffect(razeCenter, radius);

        long now = System.currentTimeMillis();

        for (Entity entity : world.getNearbyEntities(razeCenter, radius, 2.5, radius)) {
            if (!(entity instanceof LivingEntity victim)
                    || entity instanceof ArmorStand
                    || entity.equals(player)) {
                continue;
            }

            if (victim instanceof Player targetPlayer) {
                Hero targetHero = HeroManager.getHero(targetPlayer);
                if (targetHero != null && targetHero.getTeam().equalsIgnoreCase(hero.getTeam())) {
                    continue;
                }
            }

            int currentStacks = getActiveStacks(victim, now);
            double totalDamage = baseDamage + (currentStacks * bonusPerStack);

            DamageManager.dealDamageFromAbility(hero, victim, totalDamage, getDamageType());

            int newStacks = currentStacks + 1;
            victim.setMetadata(META_STACKS, new FixedMetadataValue(DotaCraft.getInstance(), newStacks));
            victim.setMetadata(META_EXPIRE, new FixedMetadataValue(DotaCraft.getInstance(), now + (long) (STACK_DURATION_SECONDS * 1000L)));
        }

        return true;
    }

    private int getActiveStacks(LivingEntity entity, long now) {
        if (!entity.hasMetadata(META_STACKS) || !entity.hasMetadata(META_EXPIRE)) {
            return 0;
        }
        long expire = entity.getMetadata(META_EXPIRE).get(0).asLong();
        if (now > expire) {
            return 0;
        }
        return entity.getMetadata(META_STACKS).get(0).asInt();
    }

    private void drawRazeEffect(Location center, double radius) {
        World world = center.getWorld();
        if (world == null) return;

        Particle.DustOptions crimson = new Particle.DustOptions(Color.fromRGB(200, 15, 15), 1.4f);
        Particle.DustOptions darkShadow = new Particle.DustOptions(Color.fromRGB(20, 5, 10), 1.6f);

        for (double y = 0; y <= 2.8; y += 0.35) {
            Location pillarLoc = center.clone().add(0, y, 0);
            world.spawnParticle(Particle.DUST, pillarLoc, 6, 0.35, 0.1, 0.35, 0.0, darkShadow);
            world.spawnParticle(Particle.DUST, pillarLoc, 4, 0.25, 0.1, 0.25, 0.0, crimson);
            world.spawnParticle(Particle.SMOKE, pillarLoc, 3, 0.3, 0.1, 0.3, 0.02);
        }

        int points = 32;
        double visualRadius = radius * 1;
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;

            Location edge = center.clone().add(Math.cos(angle) * visualRadius, 0.2, Math.sin(angle) * visualRadius);
            world.spawnParticle(Particle.DUST, edge, 2, 0.05, 0.05, 0.05, 0.0, crimson);
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, edge, 1, 0.0, 0.05, 0.0, 0.01);
        }
    }
}