package com.fred4106.improvedCharges.item.listeners;

import net.runelite.api.events.GraphicChanged;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

public class ListenerOnGraphicChanged extends ListenerBase {
    public ListenerOnGraphicChanged(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger(final GraphicChanged event) {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase, event)) continue;
            final OnGraphicChanged trigger = (OnGraphicChanged) triggerBase;
            boolean triggerUsed = false;

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase, final GraphicChanged event) {
        if (!(triggerBase instanceof OnGraphicChanged)) return false;
        final OnGraphicChanged trigger = (OnGraphicChanged) triggerBase;

        // Player check.
        if (event.getActor() != provider.client.getLocalPlayer()) {
            return false;
        }

        // Graphic id check.
        graphicIdCheck: if (trigger.graphicId != null) {
            for (final int graphicId : trigger.graphicId) {
                if (event.getActor().hasSpotAnim(graphicId)) {
                    break graphicIdCheck;
                }
            }

            return false;
        }

        return super.isValidTrigger(trigger);
    }
}
