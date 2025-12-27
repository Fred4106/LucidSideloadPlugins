package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class VeracsHelm extends _BarrowsItem {
    public VeracsHelm(final Provider provider) {
        super("Verac's helmet", ItemId.VERACS_HELM, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.VERACS_HELM).fixedCharges(1000),
            new TriggerItem(ItemId.VERACS_HELM_100),
            new TriggerItem(ItemId.VERACS_HELM_75),
            new TriggerItem(ItemId.VERACS_HELM_50),
            new TriggerItem(ItemId.VERACS_HELM_25),
            new TriggerItem(ItemId.VERACS_HELM_0).fixedCharges(0)
        };
    }
}