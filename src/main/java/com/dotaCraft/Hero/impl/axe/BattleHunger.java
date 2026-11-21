package com.dotaCraft.Hero.impl.axe;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.DotaCraft;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.DamageManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;

import java.nio.charset.StandardCharsets;

public class BattleHunger extends Ability {

    public static final String ICON = ""; //TODO
    private static final String BATTLE_HUNGER_ICON = "\uE510";
    private static final Key DOTA_FONT = Key.key("dotacraft", "default");

    private double[] victimSlownessInPercent = {11, 19, 27, 35};

    public BattleHunger() {
        super(
                DamageTypes.PHYSICAL,
                AbilityTypes.UNIT_TARGET,
                TargetTypes.ENEMY,
                DispelTypes.BASIC,
                new double[] {12, 16, 22, 26}, //per game tick
                new double[] {50, 60, 70, 80},
                new double[] {0, 0, 0, 0},
                new double[] {20, 15, 10, 5},
                new double[] {700, 775, 850, 925},
                0.8,
                new double[] {0, 0, 0, 0},
                new double[] {12, 12, 12, 12},
                1,
                4,
                0,
                false,
                false,
                true,
                ICON
        );
    }

    @Override
    public boolean cast(Hero hero) {
        Player player = hero.getPlayer();
        double range = this.getCastRange(hero.getLevel());

        org.bukkit.util.RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                range,
                0.5,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );

        if (result == null || !(result.getHitEntity() instanceof LivingEntity targetEntity)) {
            return false;
        }

        int abilityLevel = getAbilityLevel();
        int levelIndex = Math.max(0, Math.min(abilityLevel - 1, victimSlownessInPercent.length - 1));
        double slowPercent = victimSlownessInPercent[levelIndex];

        double durationTicks = this.getDuration(hero.getLevel()) * 20;
        double damagePerTick = getDamage(getAbilityLevel());

        markEntityWithBattleHunger(hero, targetEntity, durationTicks, damagePerTick, slowPercent);

        return true;
    }

    private void markEntityWithBattleHunger(Hero attacker, LivingEntity targetEntity, double duration, double damagePerTick, double slowPercent) {
        AttributeInstance speedAttr = targetEntity.getAttribute(Attribute.MOVEMENT_SPEED);
        NamespacedKey slowKey = new NamespacedKey(DotaCraft.getInstance(), "battle_hunger_slow");
        Player player = attacker.getPlayer();

        if (speedAttr != null) {
            speedAttr.removeModifier(slowKey);

            double slowMultiplier = -(slowPercent / 100.0);

            AttributeModifier slowModifier = new AttributeModifier(
                    slowKey,
                    slowMultiplier,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.ANY
            );
            speedAttr.addModifier(slowModifier);
        }

        player.getWorld().playSound(player.getLocation(), "dotacraft:axe.battle_hunger", 0.2f, 1.0f);

        new BukkitRunnable() {
            private int ticksLived = 0;

            final double baseOffset = targetEntity.getHeight() + 1;
            final double lineSpacing = 0.3;

            final TextDisplay iconDisplay = targetEntity.getWorld().spawn(
                    targetEntity.getLocation().add(0, baseOffset + lineSpacing, 0),
                    TextDisplay.class,
                    display -> {
                        display.text(Component.text(BATTLE_HUNGER_ICON).font(DOTA_FONT));

                        display.setBillboard(Display.Billboard.CENTER);

                        display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));

                        display.setShadowed(false);

                        Transformation transformation = display.getTransformation();
                        transformation.getScale().set(1.5f, 1.5f, 1.5f);
                        display.setTransformation(transformation);

                        display.setTeleportDuration(1);
                    }
            );

            Particle.DustOptions redDust = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 0.6f);

            @Override
            public void run() {
                boolean isDead = (!targetEntity.isValid() || targetEntity.isDead());

                if (isDead || ticksLived >= duration) {
                    if (speedAttr != null) {
                        speedAttr.removeModifier(slowKey);
                    }
                    cancel();
                    iconDisplay.remove();
                    return;
                }

                Location loc = targetEntity.getLocation().add(0, 0.2, 0);
                targetEntity.getWorld().spawnParticle(Particle.DUST, loc, 7, 0.5, 0.5, 0.5, 0.1, redDust);

                iconDisplay.teleport(targetEntity.getLocation().add(0, baseOffset + lineSpacing, 0));

                if (ticksLived % 20 == 0) {
                    DamageManager.dealDamageFromAbility(attacker, targetEntity, damagePerTick, getDamageType());
                }

                ticksLived++;
            }
        }.runTaskTimer(DotaCraft.getInstance(), 0L, 1L);
    }
}


