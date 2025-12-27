package com.fred4106.improvedCharges.item.listeners;

import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnStatChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

public class ListenerOnStatChanged extends ListenerBase {
    public ListenerOnStatChanged(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger(final StatChanged event) {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase, event)) continue;
            final OnStatChanged trigger = (OnStatChanged) triggerBase;
            boolean triggerUsed = false;

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase, final StatChanged event) {
        if (!(triggerBase instanceof OnStatChanged)) return false;
        final OnStatChanged trigger = (OnStatChanged) triggerBase;
        final Skill skill = event.getSkill();

        // Skill check.
        if (trigger.skill != skill) {
            return false;
        }

        return super.isValidTrigger(trigger);
    }
}
