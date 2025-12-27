package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class GuthansPlatebody extends _BarrowsItem {
    public GuthansPlatebody(final Provider provider) {
        super("Guthan's body", ItemId.GUTHANS_PLATEBODY, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.GUTHANS_PLATEBODY).fixedCharges(1000),
            new TriggerItem(ItemId.GUTHANS_PLATEBODY_100),
            new TriggerItem(ItemId.GUTHANS_PLATEBODY_75),
            new TriggerItem(ItemId.GUTHANS_PLATEBODY_50),
            new TriggerItem(ItemId.GUTHANS_PLATEBODY_25),
            new TriggerItem(ItemId.GUTHANS_PLATEBODY_0).fixedCharges(0),
        };
    }
}