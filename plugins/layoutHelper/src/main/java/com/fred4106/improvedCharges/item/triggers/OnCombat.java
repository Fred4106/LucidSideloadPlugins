package com.fred4106.improvedCharges.item.triggers;

public class OnCombat extends TriggerBase {
    public final int ticksInCombat;

    public OnCombat(final int ticksInCombat) {
        this.ticksInCombat = ticksInCombat;
    }
}
