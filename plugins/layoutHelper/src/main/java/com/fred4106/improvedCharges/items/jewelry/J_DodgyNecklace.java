package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnWidgetLoaded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_DodgyNecklace extends ChargedItem {
    public J_DodgyNecklace(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.DODGY_NECKLACE, ItemId.DODGY_NECKLACE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.DODGY_NECKLACE).needsToBeEquipped(),
        };
        
        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your dodgy necklace has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Protects.
            new OnChatMessage("Your dodgy necklace protects you. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Breaks.
            new OnChatMessage("Your dodgy necklace protects you. It then crumbles to dust.").setFixedCharges(10),

            // Break.
            new OnChatMessage("The necklace shatters. Your next dodgy necklace will start afresh from (?<charges>.+) charges.").setDynamicallyCharges(),

            new OnWidgetLoaded(219, 1, 0).text("Status: (?<charges>.+) charges? left.").setDynamically().onItemClick(),
        };
    }
}
