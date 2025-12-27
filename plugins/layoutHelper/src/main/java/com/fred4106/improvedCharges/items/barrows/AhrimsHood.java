package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class AhrimsHood extends _BarrowsItem {
    public AhrimsHood(final Provider provider) {
        super("Ahrim's hood", ItemId.AHRIMS_HOOD, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.AHRIMS_HOOD).fixedCharges(1000),
            new TriggerItem(ItemId.AHRIMS_HOOD_100),
            new TriggerItem(ItemId.AHRIMS_HOOD_75),
            new TriggerItem(ItemId.AHRIMS_HOOD_50),
            new TriggerItem(ItemId.AHRIMS_HOOD_25),
            new TriggerItem(ItemId.AHRIMS_HOOD_0).fixedCharges(0),
        };
    }
}