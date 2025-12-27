package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class ToragsHelm extends _BarrowsItem {
    public ToragsHelm(final Provider provider) {
        super("Torag's helmet", ItemId.TORAGS_HELM, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TORAGS_HELM).fixedCharges(1000),
            new TriggerItem(ItemId.TORAGS_HELM_100),
            new TriggerItem(ItemId.TORAGS_HELM_75),
            new TriggerItem(ItemId.TORAGS_HELM_50),
            new TriggerItem(ItemId.TORAGS_HELM_25),
            new TriggerItem(ItemId.TORAGS_HELM_0).fixedCharges(0)
        };
    }
}