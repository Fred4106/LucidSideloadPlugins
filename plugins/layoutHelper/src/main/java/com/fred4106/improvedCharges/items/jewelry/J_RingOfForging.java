package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_RingOfForging extends ChargedItem {
    public J_RingOfForging(Provider provider) {
        super(FredsItemChargesConfig.ring_of_forging, ItemID.RING_OF_FORGING, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.RING_OF_FORGING).needsToBeEquipped()
        };

        this.triggers.addAll(List.of(
            // Break full.
            new OnChatMessage("The ring is fully charged. There would be no point in breaking it.").onMenuOption("Break").onMenuTarget("Ring of forging").setFixedCharges(140),

            // Check.
            new OnChatMessage("You can smelt (?<charges>.+) more pieces of iron ore before a ring melts.").setDynamicallyCharges(),

            // Smelt.
            new OnChatMessage("You retrieve a bar of iron.").decreaseCharges(1),

            // Break.
            new OnChatMessage("The ring shatters. Your next ring of forging will start afresh from (?<charges>.+) charges.").setDynamicallyCharges()
        ));
    }
}
