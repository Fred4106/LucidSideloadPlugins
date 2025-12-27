package com.fred4106.improvedCharges.item.listeners;

import net.runelite.api.events.ScriptPreFired;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnScriptPreFired;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

public class ListenerOnScriptPreFired extends ListenerBase {
    public ListenerOnScriptPreFired(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger(final ScriptPreFired event) {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase, event)) continue;
            final OnScriptPreFired trigger = (OnScriptPreFired) triggerBase;
            boolean triggerUsed = false;

            if (trigger.scriptConsumer.isPresent()) {
                trigger.scriptConsumer.get().accept(event);
                triggerUsed = true;
            }

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase, final ScriptPreFired event) {
        if (!(triggerBase instanceof OnScriptPreFired)) return false;
        final OnScriptPreFired trigger = (OnScriptPreFired) triggerBase;

        // Id check.
        if (trigger.scriptId != event.getScriptId()) {
            return false;
        }

        return super.isValidTrigger(trigger);
    }
}
