package com.fred4106.improvedCharges.item.listeners;

import net.runelite.api.events.VarbitChanged;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnVarbitChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

public class ListenerOnVarbitChanged extends ListenerBase {
    public ListenerOnVarbitChanged(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger(final VarbitChanged event) {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase, event)) continue;
            final OnVarbitChanged trigger = (OnVarbitChanged) triggerBase;
            boolean triggerUsed = false;

            // Set dynamically.
            if (trigger.setDynamically.isPresent() && chargedItem instanceof ChargedItem) {
                ((ChargedItem) chargedItem).setCharges(event.getValue());
            }

            // Varbit value consumer.
            if (trigger.varbitValueConsumer.isPresent()) {
                trigger.varbitValueConsumer.get().accept(event.getValue());
                triggerUsed = true;
            }

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase, final VarbitChanged event) {
        if (!(triggerBase instanceof OnVarbitChanged)) return false;
        final OnVarbitChanged trigger = (OnVarbitChanged) triggerBase;

        // Varbit id check.
        if (event.getVarbitId() != trigger.varbitId) {
            return false;
        }

        // Varbit value check.
        if (trigger.varbitValue.isPresent() && event.getValue() != trigger.varbitValue.get()) {
            return false;
        }

        return super.isValidTrigger(trigger);
    }
}
