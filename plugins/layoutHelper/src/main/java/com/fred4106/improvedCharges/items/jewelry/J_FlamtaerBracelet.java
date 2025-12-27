package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_FlamtaerBracelet extends ChargedItem {
    public J_FlamtaerBracelet(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.FLAMTAER_BRACELET, ItemId.FLAMTAER_BRACELET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.FLAMTAER_BRACELET).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[]{
            new OnChatMessage("Your Flamtaer bracelet helps you build the temple quicker. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),
            new OnChatMessage("Your flamtaer bracelet has (?<charges>.+) charges? left.").setDynamicallyCharges(),
            new OnChatMessage("Your Flamtaer bracelet helps you build the temple quicker. It then crumbles to dust.").setFixedCharges(80),
            new OnChatMessage("The bracelet shatters. Your next Flamtaer bracelet will star afresh from 80 charges.").setFixedCharges(80)
        };
    }
}
