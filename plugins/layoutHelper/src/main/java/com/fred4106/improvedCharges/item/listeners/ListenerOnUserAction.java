package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnUserAction;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ListenerOnUserAction extends ListenerBase {
    public ListenerOnUserAction(Provider provider) {
        super(provider);
    }

    public void trigger(ChargedItemBase chargedItem) {
        for (TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(chargedItem, triggerBase)) continue;

            OnUserAction trigger = (OnUserAction) triggerBase;
            boolean triggerUsed = false;

            if (super.trigger(trigger, chargedItem)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(ChargedItemBase chargedItem, TriggerBase triggerBase) {
        if (!(triggerBase instanceof OnUserAction)) return false;
        OnUserAction trigger = (OnUserAction) triggerBase;
        return super.isValidTrigger(trigger, chargedItem);
    }
}
