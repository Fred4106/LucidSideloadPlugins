package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

public class J_DigsitePendant extends ChargedItem {
    public J_DigsitePendant(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.DIGSITE_PENDANT, ItemId.DIGSITE_PENDANT_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.DIGSITE_PENDANT_1).fixedCharges(1),
            new TriggerItem(ItemId.DIGSITE_PENDANT_2).fixedCharges(2),
            new TriggerItem(ItemId.DIGSITE_PENDANT_3).fixedCharges(3),
            new TriggerItem(ItemId.DIGSITE_PENDANT_4).fixedCharges(4),
            new TriggerItem(ItemId.DIGSITE_PENDANT_5).fixedCharges(5),
        };
    }
}
