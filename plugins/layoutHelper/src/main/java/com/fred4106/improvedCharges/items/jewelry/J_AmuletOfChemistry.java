package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnWidgetLoaded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_AmuletOfChemistry extends ChargedItem {
    public J_AmuletOfChemistry(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.AMULET_OF_CHEMISTRY, ItemId.AMULET_OF_CHEMISTRY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.AMULET_OF_CHEMISTRY).needsToBeEquipped()
        };

        this.triggers = new TriggerBase[] {
            // Check
            new OnChatMessage("Your amulet of chemistry has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Use charge
            new OnChatMessage("Your amulet of chemistry helps you create a .-dose potion. It then crumbles to dust.").setFixedCharges(5),
            new OnChatMessage("Your amulet of chemistry helps you create a .-dose potion. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Status from break dialog
            new OnWidgetLoaded(219, 1, 0).text("Status: (?<charges>.+) charges? left.").setDynamically().onItemClick(),

            // Break
            new OnChatMessage("The amulet shatters. Your next amulet of chemistry will start afresh from (?<charges>.+) charges.").setDynamicallyCharges(),
        };
    }
}
