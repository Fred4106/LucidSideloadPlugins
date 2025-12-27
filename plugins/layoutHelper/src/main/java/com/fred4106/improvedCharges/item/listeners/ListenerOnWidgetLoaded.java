package com.fred4106.improvedCharges.item.listeners;

import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.triggers.OnWidgetLoaded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

import java.util.Optional;
import java.util.regex.Matcher;

import static com.fred4106.improvedCharges.FredsItemChargesPlugin.getNumberFromCommaString;

public class ListenerOnWidgetLoaded extends ListenerBase {
    public ListenerOnWidgetLoaded(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger(final WidgetLoaded event) {
        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase, event)) continue;

            boolean triggerUsed = false;
            final OnWidgetLoaded trigger = (OnWidgetLoaded) triggerBase;
            final Optional<Widget> widget = FredsItemChargesPlugin.getWidget(provider.client, trigger.groupId, trigger.childId, trigger.subChildId);
            if (!widget.isPresent()) continue;

            if (trigger.text.isPresent()) {
                final String text = FredsItemChargesPlugin.getCleanText(widget.get().getText());
                final Matcher matcher = trigger.text.get().matcher(text);
                matcher.find();

                if (trigger.setDynamically.isPresent()) {
                    ((ChargedItem) chargedItem).setCharges(getNumberFromCommaString(matcher.group("charges")));
                    triggerUsed = true;
                }

                if (trigger.matcherConsumer.isPresent()) {
                    trigger.matcherConsumer.get().accept(matcher);
                    triggerUsed = true;
                }
            }

            if (trigger.widgetConsumer.isPresent()) {
                trigger.widgetConsumer.get().accept(widget.get());
                triggerUsed = true;
            }

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed && !trigger.multiTrigger) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase, final WidgetLoaded event) {
        if (!(triggerBase instanceof OnWidgetLoaded)) return false;
        final OnWidgetLoaded trigger = (OnWidgetLoaded) triggerBase;

        // Widget group check.
        if (event.getGroupId() != trigger.groupId) {
            return false;
        }

        // Widget existance check.
        final Optional<Widget> widget = FredsItemChargesPlugin.getWidget(provider.client, trigger.groupId, trigger.childId, trigger.subChildId);
        if (!widget.isPresent()) {
            return false;
        }

        // Text check.
        if (trigger.text.isPresent()) {
            final Matcher matcher = trigger.text.get().matcher(FredsItemChargesPlugin.getCleanText(widget.get().getText()));
            if (!matcher.find()) {
                return false;
            }
        }

        return super.isValidTrigger(trigger);
    }
}
