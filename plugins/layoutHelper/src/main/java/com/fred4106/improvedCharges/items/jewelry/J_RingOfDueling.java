package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

public class J_RingOfDueling extends ChargedItem {
    public J_RingOfDueling(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.RING_OF_DUELING, ItemId.RING_OF_DUELING_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.RING_OF_DUELING_1).fixedCharges(1),
            new TriggerItem(ItemId.RING_OF_DUELING_2).fixedCharges(2),
            new TriggerItem(ItemId.RING_OF_DUELING_3).fixedCharges(3),
            new TriggerItem(ItemId.RING_OF_DUELING_4).fixedCharges(4),
            new TriggerItem(ItemId.RING_OF_DUELING_5).fixedCharges(5),
            new TriggerItem(ItemId.RING_OF_DUELING_6).fixedCharges(6),
            new TriggerItem(ItemId.RING_OF_DUELING_7).fixedCharges(7),
            new TriggerItem(ItemId.RING_OF_DUELING_8).fixedCharges(8),
        };
    }
}
