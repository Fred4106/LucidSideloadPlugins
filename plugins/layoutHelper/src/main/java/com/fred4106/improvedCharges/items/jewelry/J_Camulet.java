package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.ids.*;

import java.util.*;

public class J_Camulet extends ChargedItem {
    public J_Camulet(Provider provider) {
        super(FredsItemChargesConfig.camulet, ItemID.CAMULET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.CAMULET),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your Camulet has one charge left.").setFixedCharges(1),
            new OnChatMessage("Your Camulet has (?<charges>.+) charges left.").setDynamicallyCharges(),

            // Empty.
            new OnChatMessage("Your Camulet has run out of charges.").setFixedCharges(0),
            new OnChatMessage("Your Camulet has no charges left.").setFixedCharges(0),

            // Recharge.
            new OnChatMessage("You recharge the Camulet using camel dung. Yuck!").setFixedCharges(4),

            // Trying to charge fully charged.
            new OnChatMessage("The Camulet is already fully charged.").setFixedCharges(4),

            // Unlimited charges.
            new OnChatMessage("The Camulet has unlimited charges.").setFixedCharges(ChargeId.UNLIMITED),

            // Replace check.
            new OnMenuEntryAdded("Check-charge").replaceOption("Check"),

            // Replace rub
            new OnMenuEntryAdded("Rub").replaceOption("Teleport")
        ));
    }
}
