package com.fred4106.improvedCharges.items.helms;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class H_CircletOfWater extends ChargedItem {
    public H_CircletOfWater(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.CIRCLET_OF_WATER, ItemId.CIRCLET_OF_WATER, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CIRCLET_OF_WATER_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.CIRCLET_OF_WATER).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[] {
            // Protect from heat.
            new OnChatMessage("Your circlet protects you from the desert heat.").decreaseCharges(1),

            // Check.
            new OnChatMessage("Your circlet has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Charge while empty.
            new OnChatMessage("You add (?<charges>.+) charges? to your circlet.$").setDynamicallyCharges(),

            // Charge while not empty.
            new OnChatMessage("You add .+ charges? to your circlet. It now has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Auto-charge.
            new OnChatMessage("The banker charges your Circlet of water using (?<waterrune>.+)x Water rune.").matcherConsumer(m -> {
                final int waterRunes = Integer.parseInt(m.group("waterrune"));
                increaseCharges(waterRunes / 5);
            }),
        };
    }
}
