package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class ToragsHammers extends _BarrowsItem {
    public ToragsHammers(final Provider provider) {
        super("Torag's weapon", ItemId.TORAGS_HAMMERS, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TORAGS_HAMMERS).fixedCharges(1000),
            new TriggerItem(ItemId.TORAGS_HAMMERS_100),
            new TriggerItem(ItemId.TORAGS_HAMMERS_75),
            new TriggerItem(ItemId.TORAGS_HAMMERS_50),
            new TriggerItem(ItemId.TORAGS_HAMMERS_25),
            new TriggerItem(ItemId.TORAGS_HAMMERS_0).fixedCharges(0)
        };
    }
}