package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.*;

public class S_Chronicle extends ChargedItem {
    public S_Chronicle(Provider provider) {
        super(FredsItemChargesConfig.chronicle, ItemID.CHRONICLE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.CHRONICLE),
        };

        this.triggers.addAll(List.of(
            // Check plural.
            new OnChatMessage("Your book has (?<charges>.+) charges? left.").setDynamicallyCharges().onItemClick(),

            // Check single.
            new OnChatMessage("You have one charge left in your book.").setFixedCharges(1).onItemClick()
        ));
    }
}
