package com.dotaCraft.UI;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Manager.HeroManager;
import com.dotaCraft.Manager.IconManager;
import com.dotaCraft.Utils.HUDUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainHUD {

    private final HeroManager heroManager;

    private final Map<UUID, Double> activeCooldowns = new HashMap<>();
    private final Map<UUID, Long> cooldownExpiration = new HashMap<>();

    private static final Key DOTA_FONT = Key.key("dotacraft", "default");
    private static final Key DEFAULT_FONT = Key.key("minecraft", "default");

    private static final String COOLDOWN_BG          = "\uE000";
    private static final String COURIER_BG           = "\uE001";
    private static final String GOLD_INDICATOR       = "\uE002";
    private static final String COURIER_BRING_ITEMS  = "\uE003";
    private static final String COURIER_INSPECT      = "\uE004";
    private static final String COURIER_SPEED_BOOST  = "\uE005";
    private static final String COURIER_SHIELD       = "\uE006";

    public MainHUD(HeroManager heroManager) {
        this.heroManager = heroManager;
    }

    public void updateHUD(Player player) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        UUID uuid = player.getUniqueId();

        IconManager.onTick(player);

        player.sendActionBar(buildActionBarComponent(hero));

        Component courierComponent       = createHudElement(DOTA_FONT, COURIER_BG,          140, 50);
        Component goldComponent          = createHudElement(DOTA_FONT, GOLD_INDICATOR,       97, 50);
        Component courierItemsComponent  = createHudElement(DOTA_FONT, COURIER_BRING_ITEMS,    97, 50);
        Component courierInspect         = createHudElement(DOTA_FONT, COURIER_INSPECT,        56, 50);
        Component courierSpeedComponent  = createHudElement(DOTA_FONT, COURIER_SPEED_BOOST,   46, 50);
        Component courierShieldComponent = createHudElement(DOTA_FONT, COURIER_SHIELD,        28, 50);

        Component buffsHUD = IconManager.renderBuffsComponent(player);

        Component positionedBuffs = Component.empty()
                .append(HUDUtils.getSpaceComponent(109))
                .append(buffsHUD);

        Component fullSubComponent = Component.empty()
                .append(positionedBuffs)
                .append(courierComponent)
                .append(goldComponent)
                .append(courierItemsComponent)
                .append(courierInspect)
                .append(courierSpeedComponent)
                .append(courierShieldComponent);

        Long expireTime = cooldownExpiration.get(uuid);
        Component mainTitleComponent = Component.empty();

        if (expireTime != null && System.currentTimeMillis() < expireTime) {
            Double remainingSeconds = activeCooldowns.get(uuid);
            if (remainingSeconds != null) {
                mainTitleComponent = buildCoolDownComponent(remainingSeconds);
            }
        }

        Title hudTitle = Title.title(
                mainTitleComponent,
                fullSubComponent,
                Title.Times.times(Duration.ZERO, Duration.ofMillis(100), Duration.ofMillis(50))
        );
        player.showTitle(hudTitle);
    }

    public void showCoolDownIcon(Player player, double remainingTime) {
        if (player == null || !player.isOnline()) return;

        UUID uuid = player.getUniqueId();
        activeCooldowns.put(uuid, remainingTime);
        cooldownExpiration.put(uuid, System.currentTimeMillis() + 1500L);
    }

    private Component buildCoolDownComponent(Double remainingSeconds) {
        String timeText = String.format(java.util.Locale.US, "%.1f", remainingSeconds);

        return createHudElement(DOTA_FONT, COOLDOWN_BG, 0, 50)
                .append(Component.text(timeText, NamedTextColor.WHITE, TextDecoration.BOLD).font(DEFAULT_FONT));
    }

    private Component buildActionBarComponent(Hero hero) {
        Component gold = Component.text(" " + hero.getCurrentGold() + "g", NamedTextColor.GOLD, TextDecoration.BOLD);

        Component stats = Component.text("    ", NamedTextColor.RED)
                .append(Component.text(hero.getStrength(), NamedTextColor.RED))
                .append(Component.text("   ", NamedTextColor.GREEN))
                .append(Component.text(hero.getAgility(), NamedTextColor.GREEN))
                .append(Component.text("   ", NamedTextColor.AQUA))
                .append(Component.text(hero.getIntellect(), NamedTextColor.AQUA));

        int currentXp = hero.getCurrentXp();
        int reqXp = hero.getRequiredXpToNextLevel(hero.getLevel());

        Component levelInfo = Component.text("   [Lvl ", NamedTextColor.YELLOW)
                .append(Component.text(hero.getLevel(), NamedTextColor.YELLOW))
                .append(Component.text(" (", NamedTextColor.GRAY))
                .append(Component.text(currentXp + "/" + reqXp, NamedTextColor.GRAY))
                .append(Component.text(" XP)]", NamedTextColor.YELLOW));

        Component abilityStatus = Component.empty();
        Ability qAbility = hero.getAbilityInSlot(0);
        if (qAbility != null) {
            abilityStatus = Component.text("   [1: ", NamedTextColor.LIGHT_PURPLE)
                    .append(getAbilityStatusComponent(qAbility))
                    .append(Component.text("]", NamedTextColor.LIGHT_PURPLE));
        }

        return Component.empty()
                .append(gold)
                .append(stats)
                .append(levelInfo)
                .append(abilityStatus);
    }

    private Component getAbilityStatusComponent(Ability ability) {
        if (ability.getAbilityLevel() <= 0) {
            return Component.text("Unlearned", NamedTextColor.GRAY);
        }
        return Component.text("Ready", NamedTextColor.GREEN);
    }

    private Component createHudElement(Key font, String iconSymbol, int offsetPx, int iconWidthPx) {
        return Component.empty()
                .append(HUDUtils.getSpaceComponent(offsetPx))
                .append(Component.text(iconSymbol).font(font))
                .append(HUDUtils.getSpaceComponent(-(offsetPx + iconWidthPx)));
    }
}