package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class W_TridentOfTheSeas extends ChargedItem {
    public W_TridentOfTheSeas(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.TRIDENT_OF_THE_SEAS, ItemId.TRIDENT_OF_THE_SEAS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TRIDENT_OF_THE_SEAS_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.TRIDENT_OF_THE_SEAS),
            new TriggerItem(ItemId.TRIDENT_OF_THE_SEAS_FULL).fixedCharges(2500),
        };

        this.triggers = new TriggerBase[] {
            // Ran out of charges.
            new OnChatMessage("Your Trident of the seas has run out of charges.").setFixedCharges(0),

            // Check, one charge left.
            new OnChatMessage("Your Trident of the seas has one charge.").setFixedCharges(1),

            // Check for charges and warning when low.
            new OnChatMessage("Your Trident of the seas( only)? has (?<charges>.+) charges( left)?.").setDynamicallyCharges(),

            // Charge to full.
            new OnChatMessage("You add .* charges? to the Trident of the seas. New total: (?<charges>.+)").setDynamicallyCharges(),

            // Attack.
            new OnGraphicChanged(1251).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Trident of the seas using (?<deathrune>.+)x Death rune.*").matcherConsumer(m -> {
                final int deathRunes = Integer.parseInt(m.group("deathrune"));
                increaseCharges(deathRunes);
            }),
        };
    }
}
