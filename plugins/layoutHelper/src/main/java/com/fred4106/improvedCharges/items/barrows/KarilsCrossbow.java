package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class KarilsCrossbow extends _BarrowsItem {
    public KarilsCrossbow(final Provider provider) {
        super("Karil's weapon", ItemId.KARILS_CROSSBOW, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.KARILS_CROSSBOW).fixedCharges(1000),
            new TriggerItem(ItemId.KARILS_CROSSBOW_100),
            new TriggerItem(ItemId.KARILS_CROSSBOW_75),
            new TriggerItem(ItemId.KARILS_CROSSBOW_50),
            new TriggerItem(ItemId.KARILS_CROSSBOW_25),
            new TriggerItem(ItemId.KARILS_CROSSBOW_0).fixedCharges(0)
        };
    }
}