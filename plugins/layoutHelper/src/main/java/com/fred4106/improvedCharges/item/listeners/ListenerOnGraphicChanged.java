package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.events.CustomGraphicChanged;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.events.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ListenerOnGraphicChanged extends ListenerBase {
    public ListenerOnGraphicChanged(Provider provider) {
        super(provider);
    }

    public void trigger(CustomGraphicChanged event, ChargedItemBase chargedItem) {
        for (TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(chargedItem, triggerBase, event)) continue;
            OnGraphicChanged trigger = (OnGraphicChanged) triggerBase;
            boolean triggerUsed = false;

            if (super.trigger(trigger, chargedItem)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(ChargedItemBase chargedItem, TriggerBase triggerBase, CustomGraphicChanged event) {
        if (!(triggerBase instanceof OnGraphicChanged)) return false;
        OnGraphicChanged trigger = (OnGraphicChanged) triggerBase;

        // Graphic id check.
        graphicIdCheck: if (trigger.graphicId != null) {
            for (int graphicId : trigger.graphicId) {
                if (event.hasGraphicId(graphicId)) {
                    break graphicIdCheck;
                }
            }

            return false;
        }

        return super.isValidTrigger(trigger, chargedItem);
    }
}
