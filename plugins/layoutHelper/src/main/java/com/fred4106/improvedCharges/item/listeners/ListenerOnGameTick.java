package com.fred4106.improvedCharges.item.listeners;

import net.runelite.api.events.GameTick;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnGameTick;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

public class ListenerOnGameTick extends ListenerBase {
    public ListenerOnGameTick(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger(final GameTick gameTick) {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase, gameTick)) continue;
            final OnGameTick trigger = (OnGameTick) triggerBase;
            boolean triggerUsed = false;

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase, final GameTick event) {
        if (!(triggerBase instanceof OnGameTick)) return false;
        final OnGameTick trigger = (OnGameTick) triggerBase;

        return super.isValidTrigger(trigger);
    }
}
