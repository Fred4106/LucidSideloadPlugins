package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class S_Chronicle extends ChargedItem {
    public S_Chronicle(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.CHRONICLE, ItemId.CHRONICLE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CHRONICLE),
        };

        this.triggers = new TriggerBase[] {
            // Check plural.
            new OnChatMessage("Your book has (?<charges>.+) charges? left.").setDynamicallyCharges().onItemClick(),

            // Check single.
            new OnChatMessage("You have one charge left in your book.").setFixedCharges(1).onItemClick(),
        };
    }
}
