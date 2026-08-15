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

public class J_FlamtaerBracelet extends ChargedItem {
    public J_FlamtaerBracelet(Provider provider) {
        super(FredsItemChargesConfig.flamtaer_bracelet, ItemID.FLAMTAER_BRACELET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.FLAMTAER_BRACELET).needsToBeEquipped(),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("Your Flamtaer bracelet helps you build the temple quicker. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),
            new OnChatMessage("Your flamtaer bracelet has (?<charges>.+) charges? left.").setDynamicallyCharges(),
            new OnChatMessage("Your Flamtaer bracelet helps you build the temple quicker. It then crumbles to dust.").setFixedCharges(80),
            new OnChatMessage("The bracelet shatters. Your next Flamtaer bracelet will star afresh from 80 charges.").setFixedCharges(80)
        ));
    }
}
