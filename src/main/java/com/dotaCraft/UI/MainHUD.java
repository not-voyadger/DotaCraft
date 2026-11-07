package com.dotaCraft.UI;

import com.dotaCraft.Ability.Ability;
import com.dotaCraft.Hero.Hero;
import com.dotaCraft.Item.Item;
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

public class MainHUD {

    private final HeroManager heroManager;

    private static final Key DOTA_FONT = Key.key("dotacraft", "default");

    private static final int[] SKILL_SLOT_OFFSETS = {-120, -96, -72, -48, -24, 0};
    private static final int[] ITEM_SLOT_OFFSETS  = {24, 48, 72, 96, 120, 144};

    public MainHUD(HeroManager heroManager) {
        this.heroManager = heroManager;
    }

    public void updateHUD(Player player) {
        Hero hero = HeroManager.getHero(player);
        if (hero == null) return;

        IconManager.onTick(player);

        player.sendActionBar(buildActionBarComponent(hero));

        Component positiveBuffsHUD = IconManager.renderPositiveBuffsComponent(player);
        Component negativeBuffsHUD = IconManager.renderNegativeBuffsComponent(player);

        Component fullSubComponent = Component.empty()
                .append(Component.empty().append(HUDUtils.getSpaceComponent(-30)).append(negativeBuffsHUD))
                .append(Component.empty().append(HUDUtils.getSpaceComponent(130)).append(positiveBuffsHUD));

        Component slotsOverlay = buildSlotsOverlay(hero);

        Title hudTitle = Title.title(
                slotsOverlay,
                fullSubComponent,
                Title.Times.times(Duration.ZERO, Duration.ofMillis(100), Duration.ofMillis(50))
        );
        player.showTitle(hudTitle);
    }

    private Component buildSlotsOverlay(Hero hero) {
        Component overlay = Component.empty();

        overlay = overlay.append(HUDUtils.getSpaceComponent(145));

        for (int i = 0; i < 6; i++) {
            Ability ability = hero.getAbilityInSlot(i);
            if (ability != null && ability.getIconSymbol() != null && !ability.getIconSymbol().isEmpty()) {
                int slotOffset = SKILL_SLOT_OFFSETS[i];
                overlay = overlay.append(HUDUtils.getSpaceComponent(slotOffset))
                        .append(Component.text(ability.getIconSymbol()).font(DOTA_FONT))
                        .append(HUDUtils.getSpaceComponent(-(slotOffset + 23)));
            }
        }

        return overlay;
    }

    private Component buildActionBarComponent(Hero hero) {
        Component gold = Component.text(" " + hero.getCurrentGold() + "g", NamedTextColor.GOLD, TextDecoration.BOLD);

        Component stats = Component.text("    ", NamedTextColor.RED)
                .append(Component.text(hero.getStrength(), NamedTextColor.RED))
                .append(Component.text("   ", NamedTextColor.GREEN))
                .append(Component.text(hero.getAgility(), NamedTextColor.GREEN))
                .append(Component.text("   ", NamedTextColor.AQUA))
                .append(Component.text(hero.getIntellect(), NamedTextColor.AQUA));

        Component levelInfo = Component.text("   [Lvl ", NamedTextColor.YELLOW)
                .append(Component.text(hero.getLevel(), NamedTextColor.YELLOW))
                .append(Component.text(" (", NamedTextColor.GRAY))
                .append(Component.text(hero.getCurrentXp() + "/" + hero.getRequiredXpToNextLevel(hero.getLevel()), NamedTextColor.GRAY))
                .append(Component.text(" XP)]", NamedTextColor.YELLOW));

        Component abilitiesSummary = Component.empty();
        for (int i = 0; i < 6; i++) {
            Ability ab = hero.getAbilityInSlot(i);
            if (ab != null) {
                abilitiesSummary = abilitiesSummary
                        .append(Component.text(" [" + (i + 1) + ":", NamedTextColor.LIGHT_PURPLE))
                        .append(getAbilityStatusComponent(ab))
                        .append(Component.text("]", NamedTextColor.LIGHT_PURPLE));
            }
        }

        return Component.empty()
                .append(gold)
                .append(stats)
                .append(levelInfo)
                .append(abilitiesSummary);
    }

    private Component getAbilityStatusComponent(Ability ability) {
        if (ability.getAbilityLevel() <= 0) {
            return Component.text("•", NamedTextColor.GRAY);
        }
        return Component.text("✓", NamedTextColor.GREEN);
    }

    private Component createHudElement(Key font, String iconSymbol, int offsetPx, int iconWidthPx) {
        return Component.empty()
                .append(HUDUtils.getSpaceComponent(offsetPx))
                .append(Component.text(iconSymbol).font(font))
                .append(HUDUtils.getSpaceComponent(-(offsetPx + iconWidthPx)));
    }
}