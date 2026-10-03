package com.dotaCraft.Bind;

public enum BindTrigger {
    // Ability Slots 0..5
    SLOT_1(BindType.ABILITY, 0),
    SLOT_2(BindType.ABILITY, 1),
    SLOT_3(BindType.ABILITY, 2),
    SLOT_4(BindType.ABILITY, 3),
    PRESS_Q(BindType.ABILITY, 4),
    PRESS_F(BindType.ABILITY, 5),

    // Item Slots 0..5
    SLOT_5(BindType.ITEM, 0),
    SLOT_6(BindType.ITEM, 1),
    SLOT_7(BindType.ITEM, 2),
    SLOT_8(BindType.ITEM, 3),
    PRESS_DOUBLE_W(BindType.ITEM, 4),
    PRESS_SHIFT(BindType.ITEM, 5);

    public enum BindType { ABILITY, ITEM }

    private final BindType type;
    private final int defaultSlot;

    BindTrigger(BindType type, int defaultSlot) {
        this.type = type;
        this.defaultSlot = defaultSlot;
    }

    public BindType getType() { return type; }
    public int getDefaultSlot() { return defaultSlot; }
}