package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.events.CustomStatChanged;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnStatChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.events.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ListenerOnStatChanged extends ListenerBase {
    public ListenerOnStatChanged(Provider provider) {
        super(provider);
    }

    public void trigger(CustomStatChanged event, ChargedItemBase chargedItem) {
        for (TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(chargedItem, triggerBase, event)) continue;
            OnStatChanged trigger = (OnStatChanged) triggerBase;
            boolean triggerUsed = false;

            if (super.trigger(trigger, chargedItem)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(ChargedItemBase chargedItem, TriggerBase triggerBase, CustomStatChanged event) {
        if (!(triggerBase instanceof OnStatChanged)) return false;
        OnStatChanged trigger = (OnStatChanged) triggerBase;

        // Skill check.
        if (trigger.skill != event.skill) {
            return false;
        }

        return super.isValidTrigger(trigger, chargedItem);
    }
}
