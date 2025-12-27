package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class DharoksPlatelegs extends _BarrowsItem {
    public DharoksPlatelegs(final Provider provider) {
        super("Dharok's legs", ItemId.DHAROKS_PLATELEGS, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.DHAROKS_PLATELEGS).fixedCharges(1000),
            new TriggerItem(ItemId.DHAROKS_PLATELEGS_100),
            new TriggerItem(ItemId.DHAROKS_PLATELEGS_75),
            new TriggerItem(ItemId.DHAROKS_PLATELEGS_50),
            new TriggerItem(ItemId.DHAROKS_PLATELEGS_25),
            new TriggerItem(ItemId.DHAROKS_PLATELEGS_0).fixedCharges(0),
        };
    }
}