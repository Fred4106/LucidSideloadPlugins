package com.fred4106.improvedCharges.items.utils;

import net.runelite.api.widgets.Widget;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItemWithStatus;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.ids.ItemContainerId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

import java.util.Optional;

public class U_BloodEssence extends ChargedItemWithStatus {
    public U_BloodEssence(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.BLOOD_ESSENCE, ItemId.BLOOD_ESSENCE_INACTIVE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BLOOD_ESSENCE_INACTIVE),
            new TriggerItem(ItemId.BLOOD_ESSENCE_ACTIVE),
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your blood essence has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Charge used.
            new OnChatMessage("You manage to extract power from the Blood Essence and craft (?<charges>.+) extra runes?.").decreaseDynamicallyCharges(),

            // Depleted.
            new OnChatMessage("Your blood essence falls apart, drained of energy.").setFixedCharges(1000).deactivate(),

            // Activate.
            new OnChatMessage("You activate the blood essence.").activate(),

            // Status from inventory.
            new OnItemContainerChanged(ItemContainerId.INVENTORY).itemsConsumer(items -> {
                if (items.hasItem(ItemId.BLOOD_ESSENCE_ACTIVE)) {
                    activate();
                } else if (items.hasItem(ItemId.BLOOD_ESSENCE_INACTIVE)) {
                    deactivate();
                }
            }),

            // Destroy.
            new OnScriptPreFired(1651).scriptConsumer((script) -> {
                final Optional<Widget> destroyWidgetItem = FredsItemChargesPlugin.getWidget(provider.client, 584, 5);
                if (
                    destroyWidgetItem.isPresent() &&
                    (destroyWidgetItem.get().getItemId() == ItemId.BLOOD_ESSENCE_ACTIVE || destroyWidgetItem.get().getItemId() == ItemId.BLOOD_ESSENCE_INACTIVE) &&
                    script.getScriptEvent().getArguments().length >= 5 &&
                    script.getScriptEvent().getArguments()[4].toString().equals("Yes")
                ) {
                    provider.store.addConsumerToNextTickQueue(() -> setCharges(1000));

                    if (destroyWidgetItem.get().getItemId() == ItemId.BLOOD_ESSENCE_ACTIVE) {
                        provider.store.addConsumerToNextTickQueue(this::deactivate);
                    }
                }
            }),
        };
    }
}
