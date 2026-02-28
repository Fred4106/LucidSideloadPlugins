package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import net.runelite.api.widgets.Widget;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;
import java.util.Optional;

public class J_RingOfEndurance extends ChargedItem {
    public J_RingOfEndurance(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.RING_OF_ENDURANCE, ItemId.RING_OF_ENDURANCE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.RING_OF_ENDURANCE),
            new TriggerItem(ItemId.RING_OF_ENDURANCE_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.RING_OF_ENDURANCE_NOCHARGES).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            // Charge.
            new OnChatMessage("You load your Ring of endurance with (?<charges>.+) stamina doses?.").increaseDynamically(),

            // Check.
            new OnChatMessage("Your Ring of endurance is charged with (?<charges>.+) stamina doses?.").setDynamicallyCharges(),

            // Use charge.
            new OnChatMessage("Your Ring of endurance doubles the duration of your stamina potion's effect.").decreaseCharges(1),

            // Uncharge.
            new OnMenuOptionClicked("Yes").runConsumerOnNextGameTick(() -> {
                final Optional<Widget> unchargeWidget = FredsItemChargesPlugin.getWidget(provider.client, 584, 0, 2);
                if (unchargeWidget.isPresent() && unchargeWidget.get().getText().equals("Are you sure you want to uncharge your Ring of endurance?")) {
                    setCharges(0);
                }
            })
        ));
    }
}