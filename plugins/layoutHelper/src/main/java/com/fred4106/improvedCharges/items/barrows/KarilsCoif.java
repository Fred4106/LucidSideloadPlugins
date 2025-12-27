package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class KarilsCoif extends _BarrowsItem {
    public KarilsCoif(final Provider provider) {
        super("Karil's coif", ItemId.KARILS_COIF, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.KARILS_COIF).fixedCharges(1000),
            new TriggerItem(ItemId.KARILS_COIF_100),
            new TriggerItem(ItemId.KARILS_COIF_75),
            new TriggerItem(ItemId.KARILS_COIF_50),
            new TriggerItem(ItemId.KARILS_COIF_25),
            new TriggerItem(ItemId.KARILS_COIF_0).fixedCharges(0)
        };
    }
}