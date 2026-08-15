package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnScriptPreFired;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import net.runelite.api.widgets.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_BindingNecklace extends ChargedItem {
    public J_BindingNecklace(Provider provider) {
        super(FredsItemChargesConfig.binding_necklace, ItemID.MAGIC_EMERALD_NECKLACE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.MAGIC_EMERALD_NECKLACE).needsToBeEquipped(),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("You have (?<charges>.+) charges? left before your Binding necklace disintegrates.").setDynamicallyCharges(),

            // Charge used.
            new OnChatMessage("You bind the temple's power into (Mud|Lava|Steam|Dust|Smoke|Mist|Aether) runes?.").decreaseCharges(1),

            // Fully used.
            new OnChatMessage("Your Binding necklace has disintegrated.").runConsumerOnNextGameTick(() -> setCharges(16)),

            // Destroy.
            new OnScriptPreFired(1651).scriptConsumer((script) -> {
                Optional<Widget> destroyWidget = FredsItemChargesPlugin.getWidget(provider.client, 584, 0, 2);
                if (
                    destroyWidget.isPresent() && destroyWidget.get().getText().equals("Destroy necklace of binding?") &&
                    script.arguments.length >= 5 &&
                    script.arguments[4].toString().equals("Yes")
                ) {
                    provider.store.addConsumerToNextTickQueue(() -> setCharges(16));
                }
            })
        ));
    }
}
