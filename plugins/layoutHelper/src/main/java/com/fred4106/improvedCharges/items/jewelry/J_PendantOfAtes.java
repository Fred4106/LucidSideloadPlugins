package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

public class J_PendantOfAtes extends ChargedItem {
    public J_PendantOfAtes(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.PENDANT_OF_ATES, ItemId.PENDANT_OF_ATES, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.PENDANT_OF_ATES_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.PENDANT_OF_ATES),
        };

        this.triggers = new TriggerBase[]{
            // Check empty.
            new OnChatMessage("The pendant has no charges.").setFixedCharges(0).onItemClick(),

            // Check.
            new OnChatMessage("The pendant has (?<charges>.+) charges?.").setDynamicallyCharges().onItemClick(),

            // Charge.
            new OnChatMessage("You add .+ frozen tears? to your pendant. It now has (?<charges>.+) charges.").setDynamicallyCharges(),

            // Uncharge.
            new OnChatMessage("You uncharge your pendant by removing (?<charges>.+) frozen tears? from it.").decreaseDynamicallyCharges(),

            // Teleport.
            new OnGraphicChanged(2754).decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Pendant of ates using (?<frozentear>.+)x Frozen tear.").matcherConsumer(m -> {
                final int frozenTear = Integer.parseInt(m.group("frozentear"));
                increaseCharges(frozenTear);
            }),

            // Unified menu entry.
            new OnMenuEntryAdded("Rub").replaceOption("Teleport"),
        };
    }
}
