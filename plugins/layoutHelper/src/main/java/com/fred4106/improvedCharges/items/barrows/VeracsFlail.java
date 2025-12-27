package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class VeracsFlail extends _BarrowsItem {
    public VeracsFlail(final Provider provider) {
        super("Verac's weapon", ItemId.VERACS_FLAIL, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.VERACS_FLAIL).fixedCharges(1000),
            new TriggerItem(ItemId.VERACS_FLAIL_100),
            new TriggerItem(ItemId.VERACS_FLAIL_75),
            new TriggerItem(ItemId.VERACS_FLAIL_50),
            new TriggerItem(ItemId.VERACS_FLAIL_25),
            new TriggerItem(ItemId.VERACS_FLAIL_0).fixedCharges(0)
        };
    }
}