package com.fred4106.improvedCharges.item.triggers;

public class OnCombat extends TriggerBase {
    public int ticksInCombat;

    public OnCombat(int ticksInCombat) {
        this.ticksInCombat = ticksInCombat;
    }
}
