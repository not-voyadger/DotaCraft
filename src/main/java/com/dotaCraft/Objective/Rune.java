package com.dotaCraft.Objective;

import com.dotaCraft.Item.Item;
import com.dotaCraft.Item.impl.Bottle;
import com.dotaCraft.Manager.MessageManager;
import org.bukkit.Location;
import com.dotaCraft.Hero.Hero;
import org.bukkit.entity.Player;

public class Rune extends Objective {

    public enum RuneType {DOUBLE_DAMAGE, HASTE, SHIELD, REGENERATION, INVISIBILITY, ILLUSION, ARCANE};

    final Location location;
    private final RuneType type;

    public Rune(Location location, RuneType type) {
        super(
                "rune",
                null,
                0,
                0,
                0,
                0,
                0,
                location,
                0
        );
        this.location = location;
        this.type = type;
    }

    @Override
    public void spawn(Location loc) {
        // TODO: spawn logic
    }

    public static Rune spawn(Location loc, RuneType type) {
        Rune rune = new Rune(loc, type);
        return rune;
    }

    public RuneType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    protected void updateNameTag() {

    }

    private void claimRune(Hero hero, Rune rune) {
        Player player = hero.getPlayer();

        if (hero == null || rune == null) return;

        for (int slot = 0; slot < 6; slot++) {
            Item item = hero.getItemInSlot(slot);

            if (item instanceof Bottle bottle && bottle.storedRune == null) {

                RuneType runeToStore = rune.getType();
                bottle.storeRune(runeToStore);

                MessageManager.broadcastPlayerStoringRuneToBottle(player, hero.getTeam(), runeToStore);

                // TODO: update bottle icon depending on rune

                if (hero.getPlayer() != null) {
                    player.getWorld().playSound(player.getLocation(), "dotacraft:items.bottle_store", 0.5f, 1.0f);
                }
                return;
            }
        }

        Rune.activateRune(hero, rune.getType());
    }

    public static void activateRune(Hero hero, RuneType type) {
        if (hero == null || type == null) return;
        // TODO
    }
}
