package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.triggers.OnVarbitChanged;
import com.fred4106.improvedCharges.item.triggers.OnVarbitsMapChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.events.VarbitChanged;

public class ListenerOnVarbitsMapChanged extends ListenerBase {
    public ListenerOnVarbitsMapChanged(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger(final VarbitChanged event) {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase, event)) continue;
            final OnVarbitsMapChanged trigger = (OnVarbitsMapChanged) triggerBase;
            final ChargedItemWithStorage chargedItem = (ChargedItemWithStorage) this.chargedItem;
            boolean triggerUsed = false;

            chargedItem.storage.put(trigger.varbitsMap.get(event.getVarbitId()), event.getValue());
            triggerUsed = true;

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase, final VarbitChanged event) {
        if (!(triggerBase instanceof OnVarbitsMapChanged)) return false;
        if (!(chargedItem instanceof ChargedItemWithStorage)) return false;

        final OnVarbitsMapChanged trigger = (OnVarbitsMapChanged) triggerBase;

        // Valid varbit id check.
        if (!trigger.varbitsMap.containsKey(event.getVarbitId())) {
            return false;
        }

        return super.isValidTrigger(trigger);
    }
}