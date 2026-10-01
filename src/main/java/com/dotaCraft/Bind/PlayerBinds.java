package com.dotaCraft.Bind;

import java.util.EnumMap;
import java.util.Map;

public class PlayerBinds {

    public static class BindTarget {
        private final BindTrigger.BindType type;
        private final int targetSlot;

        public BindTarget(BindTrigger.BindType type, int targetSlot) {
            this.type = type;
            this.targetSlot = targetSlot;
        }

        public BindTrigger.BindType getType() { return type; }
        public int getTargetSlot() { return targetSlot; }
    }

    private final Map castBinds = new EnumMap<>(BindTrigger.class);

    public PlayerBinds() {
        for (BindTrigger trigger : BindTrigger.values()) {
            castBinds.put(trigger, new BindTarget(trigger.getType(), trigger.getDefaultSlot()));
        }
    }

    public BindTarget getBindTarget(BindTrigger trigger) {
        return (BindTarget) castBinds.get(trigger);
    }

    public boolean hasTrigger(BindTrigger trigger) {
        return castBinds.containsKey(trigger);
    }

    public void setBind(BindTrigger trigger, BindTrigger.BindType type, int targetSlot) {
        castBinds.put(trigger, new BindTarget(type, targetSlot));
    }
}