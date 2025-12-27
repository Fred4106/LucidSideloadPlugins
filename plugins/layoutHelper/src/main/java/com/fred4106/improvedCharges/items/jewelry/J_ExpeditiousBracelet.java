package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_ExpeditiousBracelet extends ChargedItem {
    public J_ExpeditiousBracelet(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.EXPEDITIOUS_BRACELET, ItemId.EXPEDITIOUS_BRACELET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.EXPEDITIOUS_BRACELET).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your expeditious bracelet has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Charge used.
            new OnChatMessage("Your expeditious bracelet helps you progress your slayer( task)? faster. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Bracelet fully used.
            new OnChatMessage("Your expeditious bracelet helps you progress your slayer task faster. It then crumbles to dust.").setFixedCharges(30),

            // Break.
            new OnChatMessage("The bracelet shatters. Your next expeditious bracelet will start afresh from (?<charges>.+) charges.").setDynamicallyCharges(),
        };
    }
}
