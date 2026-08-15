package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.events.CustomScriptPreFired;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnScriptPreFired;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.events.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ListenerOnScriptPreFired extends ListenerBase {
    public ListenerOnScriptPreFired(Provider provider) {
        super(provider);
    }

    public void trigger(CustomScriptPreFired event, ChargedItemBase chargedItem) {
        for (TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(chargedItem, triggerBase, event)) continue;
            OnScriptPreFired trigger = (OnScriptPreFired) triggerBase;
            boolean triggerUsed = false;

            if (trigger.scriptConsumer.isPresent()) {
                trigger.scriptConsumer.get().accept(event);
                triggerUsed = true;
            }

            if (super.trigger(trigger, chargedItem)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(ChargedItemBase chargedItem, TriggerBase triggerBase, CustomScriptPreFired event) {
        if (!(triggerBase instanceof OnScriptPreFired)) return false;
        OnScriptPreFired trigger = (OnScriptPreFired) triggerBase;

        // Id check.
        if (trigger.scriptId != event.scriptId) {
            return false;
        }

        return super.isValidTrigger(trigger, chargedItem);
    }
}
