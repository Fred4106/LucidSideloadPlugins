package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.events.CustomMenuOptionClicked;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnItemUsed;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.events.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ListenerOnItemUsed extends ListenerBase {
    public ListenerOnItemUsed(Provider provider) {
        super(provider);
    }

    public void trigger(CustomMenuOptionClicked event, ChargedItemBase chargedItem) {
        for (TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(chargedItem, triggerBase, event)) continue;

            OnItemUsed triggerOnItemUsed = (OnItemUsed) triggerBase;
            boolean triggerUsed = false;

            if (super.trigger(triggerOnItemUsed, chargedItem)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    private boolean isValidTrigger(ChargedItemBase chargedItem, TriggerBase triggerBase, CustomMenuOptionClicked event) {
        if (!(triggerBase instanceof OnItemUsed)) return false;
        if (event.usedItemId.isEmpty()) return false;
        OnItemUsed triggerOnItemUsed = (OnItemUsed) triggerBase;

        if (!(
            (event.itemId == triggerOnItemUsed.targetItemId && event.usedItemId.get() == triggerOnItemUsed.usedItemId) ||
            (triggerOnItemUsed.isBothWays && event.itemId == event.usedItemId.get() && event.usedItemId.get() == triggerOnItemUsed.targetItemId)
        )) {
            return false;
        }

        return super.isValidTrigger(triggerBase, chargedItem);
    }
}
