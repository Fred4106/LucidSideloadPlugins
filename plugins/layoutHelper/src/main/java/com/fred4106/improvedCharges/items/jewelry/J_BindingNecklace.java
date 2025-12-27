package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import net.runelite.api.widgets.Widget;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.Optional;

public class J_BindingNecklace extends ChargedItem {
    public J_BindingNecklace(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.BINDING_NECKLACE, ItemId.BINDING_NECKLACE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BINDING_NECKLACE).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[] {
            // Check, one left.
            new OnChatMessage("You have one charge left before your Binding necklace disintegrates.").setFixedCharges(1),

            // Check.
            new OnChatMessage("You have (?<charges>.+) charges left before your Binding necklace disintegrates.").setDynamicallyCharges(),

            // Charge used.
            new OnChatMessage("You (partially succeed to )?bind the temple's power into (Mud|Lava|Steam|Dust|Smoke|Mist|Aether) runes?.").decreaseCharges(1),

            // Fully used.
            new OnChatMessage("Your Binding necklace has disintegrated.").runConsumerOnNextGameTick(() -> setCharges(16)),

            // Destroy.
            new OnScriptPreFired(1651).scriptConsumer((script) -> {
                final Optional<Widget> destroyWidget = FredsItemChargesPlugin.getWidget(provider.client, 584, 0, 2);
                if (
                    destroyWidget.isPresent() && destroyWidget.get().getText().equals("Destroy necklace of binding?") &&
                    script.getScriptEvent().getArguments().length >= 5 &&
                    script.getScriptEvent().getArguments()[4].toString().equals("Yes")
                ) {
                    provider.store.addConsumerToNextTickQueue(() -> setCharges(16));
                }
            }),
        };
    }
}
