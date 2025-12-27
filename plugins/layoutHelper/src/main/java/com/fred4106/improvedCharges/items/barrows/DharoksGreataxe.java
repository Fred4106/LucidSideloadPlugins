package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class DharoksGreataxe extends _BarrowsItem {
    public DharoksGreataxe(final Provider provider) {
        super("Dharok's weapon", ItemId.DHAROKS_GREATAXE, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.DHAROKS_GREATAXE).fixedCharges(1000),
            new TriggerItem(ItemId.DHAROKS_GREATAXE_100),
            new TriggerItem(ItemId.DHAROKS_GREATAXE_75),
            new TriggerItem(ItemId.DHAROKS_GREATAXE_50),
            new TriggerItem(ItemId.DHAROKS_GREATAXE_25),
            new TriggerItem(ItemId.DHAROKS_GREATAXE_0).fixedCharges(0),
        };
    }
}