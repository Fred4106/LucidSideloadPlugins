package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class W_SkullSceptre extends ChargedItem {
    public W_SkullSceptre(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.SKULL_SCEPTRE, ItemId.SKULL_SCEPTRE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.SKULL_SCEPTRE),
            new TriggerItem(ItemId.SKULL_SCEPTRE_IMBUED)
        };

        this.triggers = new TriggerBase[] {
            // Teleport.
            new OnChatMessage("Your Skull Sceptre has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Check.
            new OnChatMessage("Concentrating deeply, you divine that the sceptre has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Charge to maximum without all Varrock diaries completed.
            new OnChatMessage("You charge the Skull Sceptre with .*. It now contains the maximum number of charges, (?<charges>.+). Completing tiers of the Varrock achievement diary will allow the imbued Skull Sceptre to hold more charges.").setDynamicallyCharges(),

            // Charge to maximum.
            new OnChatMessage("You charge the Skull Sceptre with .*. It now contains the maximum number of charges, (?<charges>.+).").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You charge the Skull Sceptre with .*. It now contains (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Unified menu entry.
            new OnMenuEntryAdded("Divine").replaceOption("Check"),
            new OnMenuEntryAdded("Invoke").replaceOption("Teleport"),
        };
    }
}
