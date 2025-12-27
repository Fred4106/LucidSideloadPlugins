package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class U_FungicideSpray extends ChargedItem {
    public U_FungicideSpray(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.FUNGICIDE_SPRAY, ItemId.FUNGICIDE_SPRAY_0, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_0).fixedCharges(0),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_1).fixedCharges(1),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_2).fixedCharges(2),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_3).fixedCharges(3),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_4).fixedCharges(4),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_5).fixedCharges(5),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_6).fixedCharges(6),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_7).fixedCharges(7),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_8).fixedCharges(8),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_9).fixedCharges(9),
            new TriggerItem(ItemId.FUNGICIDE_SPRAY_10).fixedCharges(10),
        };
    }
}
