package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class KarilsLeathertop extends _BarrowsItem {
    public KarilsLeathertop(final Provider provider) {
        super("Karil's body", ItemId.KARILS_LEATHERTOP, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.KARILS_LEATHERTOP).fixedCharges(1000),
            new TriggerItem(ItemId.KARILS_LEATHERTOP_100),
            new TriggerItem(ItemId.KARILS_LEATHERTOP_75),
            new TriggerItem(ItemId.KARILS_LEATHERTOP_50),
            new TriggerItem(ItemId.KARILS_LEATHERTOP_25),
            new TriggerItem(ItemId.KARILS_LEATHERTOP_0).fixedCharges(0)
        };
    }
}
