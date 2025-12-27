package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class J_RingOfWealth extends ChargedItem {
    public J_RingOfWealth(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.RING_OF_WEALTH, ItemId.RING_OF_WEALTH_0, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.RING_OF_WEALTH_0).fixedCharges(0),
            new TriggerItem(ItemId.RING_OF_WEALTH_1).fixedCharges(1),
            new TriggerItem(ItemId.RING_OF_WEALTH_2).fixedCharges(2),
            new TriggerItem(ItemId.RING_OF_WEALTH_3).fixedCharges(3),
            new TriggerItem(ItemId.RING_OF_WEALTH_4).fixedCharges(4),
            new TriggerItem(ItemId.RING_OF_WEALTH_5).fixedCharges(5),
            new TriggerItem(ItemId.RING_OF_WEALTH_IMBUED_0).fixedCharges(0),
            new TriggerItem(ItemId.RING_OF_WEALTH_IMBUED_1).fixedCharges(1),
            new TriggerItem(ItemId.RING_OF_WEALTH_IMBUED_2).fixedCharges(2),
            new TriggerItem(ItemId.RING_OF_WEALTH_IMBUED_3).fixedCharges(3),
            new TriggerItem(ItemId.RING_OF_WEALTH_IMBUED_4).fixedCharges(4),
            new TriggerItem(ItemId.RING_OF_WEALTH_IMBUED_5).fixedCharges(5),
        };
    }
}
