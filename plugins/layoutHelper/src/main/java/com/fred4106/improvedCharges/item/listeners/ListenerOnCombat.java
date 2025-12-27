package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

public class ListenerOnCombat extends ListenerBase {
    private int ticksInCombat = 0;

    public ListenerOnCombat(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger() {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase)) continue;
            final OnCombat trigger = (OnCombat) triggerBase;
            boolean triggerUsed = false;

            if (trigger.ticksInCombat == ticksInCombat) {
                triggerUsed = true;
                ticksInCombat = 0;
            }

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase) {
        if (!(triggerBase instanceof OnCombat)) return false;

        // Ticks check.
        if (++ticksInCombat != ((OnCombat) triggerBase).ticksInCombat) {
            return false;
        }

        return super.isValidTrigger(triggerBase);
    }
}
