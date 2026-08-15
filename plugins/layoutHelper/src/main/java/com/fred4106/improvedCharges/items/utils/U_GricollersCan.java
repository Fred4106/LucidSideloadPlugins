package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class U_GricollersCan extends ChargedItem {
    public U_GricollersCan(Provider provider) {
        super(FredsItemChargesConfig.gricollers_can, ItemID.ZEAH_WATERINGCAN, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.ZEAH_WATERINGCAN),
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
