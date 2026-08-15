package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.Provider;

public class U_FungicideSpray extends ChargedItem {
    public U_FungicideSpray(Provider provider) {
        super(FredsItemChargesConfig.fungicide_spray, ItemID.SLAYER_SPRAY_PUMP_0, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_0).fixedCharges(0),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_1).fixedCharges(1),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_2).fixedCharges(2),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_3).fixedCharges(3),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_4).fixedCharges(4),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_5).fixedCharges(5),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_6).fixedCharges(6),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_7).fixedCharges(7),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_8).fixedCharges(8),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_9).fixedCharges(9),
            new TriggerItem(ItemID.SLAYER_SPRAY_PUMP_10).fixedCharges(10),
        };
    }
}
