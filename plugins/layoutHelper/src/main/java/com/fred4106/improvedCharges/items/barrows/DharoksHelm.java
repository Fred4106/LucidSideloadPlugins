package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class DharoksHelm extends _BarrowsItem {
    public DharoksHelm(final Provider provider) {
        super("Dharok's helmet", ItemId.DHAROKS_HELM, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.DHAROKS_HELM).fixedCharges(1000),
            new TriggerItem(ItemId.DHAROKS_HELM_100),
            new TriggerItem(ItemId.DHAROKS_HELM_75),
            new TriggerItem(ItemId.DHAROKS_HELM_50),
            new TriggerItem(ItemId.DHAROKS_HELM_25),
            new TriggerItem(ItemId.DHAROKS_HELM_0).fixedCharges(0),
        };
    }
}