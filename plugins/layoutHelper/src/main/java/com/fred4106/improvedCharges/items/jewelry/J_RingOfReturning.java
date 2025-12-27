package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class J_RingOfReturning extends ChargedItem {
    public J_RingOfReturning(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.RING_OF_RETURNING, ItemId.RING_OF_RETURNING_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.RING_OF_RETURNING_1).fixedCharges(1),
            new TriggerItem(ItemId.RING_OF_RETURNING_2).fixedCharges(2),
            new TriggerItem(ItemId.RING_OF_RETURNING_3).fixedCharges(3),
            new TriggerItem(ItemId.RING_OF_RETURNING_4).fixedCharges(4),
            new TriggerItem(ItemId.RING_OF_RETURNING_5).fixedCharges(5),
        };
    }
}
