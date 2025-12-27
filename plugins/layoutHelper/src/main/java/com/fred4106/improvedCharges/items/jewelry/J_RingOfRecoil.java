package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import com.fred4106.improvedCharges.store.Provider;

public class J_RingOfRecoil extends ChargedItem {
    public J_RingOfRecoil(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.RING_OF_RECOIL, ItemId.RING_OF_RECOIL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.RING_OF_RECOIL).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("You can inflict one more point of damage before a ring will shatter.").setFixedCharges(1),

            // Check.
            new OnChatMessage("You can inflict (?<charges>.+) more points of damage before a ring will shatter.").setDynamicallyCharges(),

            // Trying to break when full.
            new OnChatMessage("The ring is fully charged. There would be no point in breaking it.").onItemClick().setFixedCharges(40),

            // Shattered.
            new OnChatMessage("Your Ring of Recoil has shattered.").setFixedCharges(40),

            // Take damage.
            new OnHitsplatApplied(HitsplatTarget.SELF, HitsplatGroup.SUCCESSFUL).moreThanZeroDamage().isEquipped().decreaseCharges(1),

            // Check from break dialog.
            new OnWidgetLoaded(219, 1, 0).text("Status: (?<charges>.+) damage points? left.").setDynamically(),

            // Break.
            new OnChatMessage("The ring shatters. Your next ring of recoil will start afresh from (?<charges>.+) damage points?.").setDynamicallyCharges(),
        };
    }
}
