package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_BraceletOfSlaughter extends ChargedItem {
    public J_BraceletOfSlaughter(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.BRACELET_OF_SLAUGHTER, ItemId.BRACELET_OF_SLAUGHTER, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BRACELET_OF_SLAUGHTER).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your bracelet of slaughter has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Charge used.
            new OnChatMessage("Your bracelet of slaughter prevents your slayer count from decreasing. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Bracelet fully used.
            new OnChatMessage("Your bracelet of slaughter prevents your slayer count from decreasing. It then crumbles to dust.").setFixedCharges(30),

            // Break.
            new OnChatMessage("The bracelet shatters. Your next bracelet of slaughter will start afresh from (?<charges>.+) charges.").setDynamicallyCharges(),
        };
    }
}
