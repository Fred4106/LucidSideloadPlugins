package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_SlayerRing extends ChargedItem {
    public J_SlayerRing(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.SLAYER_RING, ItemId.SLAYER_RING_8, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.SLAYER_RING_1).fixedCharges(1),
            new TriggerItem(ItemId.SLAYER_RING_2).fixedCharges(2),
            new TriggerItem(ItemId.SLAYER_RING_3).fixedCharges(3),
            new TriggerItem(ItemId.SLAYER_RING_4).fixedCharges(4),
            new TriggerItem(ItemId.SLAYER_RING_5).fixedCharges(5),
            new TriggerItem(ItemId.SLAYER_RING_6).fixedCharges(6),
            new TriggerItem(ItemId.SLAYER_RING_7).fixedCharges(7),
            new TriggerItem(ItemId.SLAYER_RING_8).fixedCharges(8),
        };
    }
}
