package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class U_Waterskin extends ChargedItem {
    public U_Waterskin(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.WATERSKIN, ItemId.WATERSKIN_0, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.WATERSKIN_0).fixedCharges(0),
            new TriggerItem(ItemId.WATERSKIN_1).fixedCharges(1),
            new TriggerItem(ItemId.WATERSKIN_2).fixedCharges(2),
            new TriggerItem(ItemId.WATERSKIN_3).fixedCharges(3),
            new TriggerItem(ItemId.WATERSKIN_4).fixedCharges(4),
        };
    }
}
