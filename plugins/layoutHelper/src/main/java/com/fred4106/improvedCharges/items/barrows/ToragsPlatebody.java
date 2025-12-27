package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class ToragsPlatebody extends _BarrowsItem {
    public ToragsPlatebody(final Provider provider) {
        super("Torag's body", ItemId.TORAGS_PLATEBODY, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TORAGS_PLATEBODY).fixedCharges(1000),
            new TriggerItem(ItemId.TORAGS_PLATEBODY_100),
            new TriggerItem(ItemId.TORAGS_PLATEBODY_75),
            new TriggerItem(ItemId.TORAGS_PLATEBODY_50),
            new TriggerItem(ItemId.TORAGS_PLATEBODY_25),
            new TriggerItem(ItemId.TORAGS_PLATEBODY_0).fixedCharges(0)
        };
    }
}