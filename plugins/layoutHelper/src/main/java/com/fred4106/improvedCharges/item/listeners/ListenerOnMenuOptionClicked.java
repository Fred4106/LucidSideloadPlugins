package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.events.CustomMenuOptionClicked;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnMenuOptionClicked;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.events.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ListenerOnMenuOptionClicked extends ListenerBase {
    public ListenerOnMenuOptionClicked(Provider provider) {
        super(provider);
    }

    public void trigger(CustomMenuOptionClicked event, ChargedItemBase chargedItem) {
        for (TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(chargedItem, triggerBase, event)) continue;
            OnMenuOptionClicked trigger = (OnMenuOptionClicked) triggerBase;
            boolean triggerUsed = false;

            if (trigger.menuOptionConsumer.isPresent()) {
                trigger.menuOptionConsumer.get().accept(event);
                triggerUsed = true;
            }

            if (super.trigger(trigger, chargedItem)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(ChargedItemBase chargedItem, TriggerBase triggerBase, CustomMenuOptionClicked event) {
        if (!(triggerBase instanceof OnMenuOptionClicked)) return false;
        OnMenuOptionClicked trigger = (OnMenuOptionClicked) triggerBase;

        // Option check.
        boolean optionCheck = false;
        for (String option : trigger.options) {
            if (event.option.equals(option)) {
                optionCheck = true;
                break;
            }
        }
        if (!optionCheck) return false;

        // Item id check.
        if (trigger.hasItemId.isPresent() && event.itemId != trigger.hasItemId.get()) {
            return false;
        }

        return super.isValidTrigger(trigger, chargedItem);
    }
}
