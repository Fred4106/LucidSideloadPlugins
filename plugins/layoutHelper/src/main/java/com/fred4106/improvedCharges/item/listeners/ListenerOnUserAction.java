package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnUserAction;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

public class ListenerOnUserAction extends ListenerBase {
    public ListenerOnUserAction(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger() {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase)) continue;

            final OnUserAction trigger = (OnUserAction) triggerBase;
            boolean triggerUsed = false;

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase) {
        if (!(triggerBase instanceof OnUserAction)) return false;
        final OnUserAction trigger = (OnUserAction) triggerBase;
        return super.isValidTrigger(trigger);
    }
}
