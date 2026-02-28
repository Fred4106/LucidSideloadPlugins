package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class U_GricollersCan extends ChargedItem {
    public U_GricollersCan(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.GRICOLLERS_CAN, ItemId.GRICOLLERS_CAN, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.GRICOLLERS_CAN),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Watering can charges remaining: (?<charges>.+)%").setDynamicallyCharges().onItemClick(),

            // Water inventory item.
            new OnChatMessage("You water").onItemClick().decreaseCharges(1),

            // Fill.
            new OnChatMessage("You fill the watering can").onItemClick().setFixedCharges(1000),

            // Water.
            new OnGraphicChanged(410).decreaseCharges(1)
        ));
    }
}
