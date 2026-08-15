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

public class J_AmuletOfBounty extends ChargedItem {
    public J_AmuletOfBounty(Provider provider) {
        super(FredsItemChargesConfig.amulet_of_bounty, ItemID.AMULET_OF_BOUNTY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.AMULET_OF_BOUNTY)
        };

        this.triggers.addAll(List.of(
            // Check
            new OnChatMessage("Your amulet of bounty has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Use
            new OnChatMessage("Your amulet of bounty saves some seeds for you. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Crumbles.
            new OnChatMessage("Your amulet of bounty saves some seeds for you. It then crumbles to dust.").setFixedCharges(10),

            // Destroy
            new OnChatMessage("The amulet shatters. Your next amulet of bounty will start afresh from 10 charges.").setFixedCharges(10)
        ));
    }
}
