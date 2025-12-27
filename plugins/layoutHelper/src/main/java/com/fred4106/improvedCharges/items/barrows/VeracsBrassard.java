package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class VeracsBrassard extends _BarrowsItem {
    public VeracsBrassard(final Provider provider) {
        super("Verac's body", ItemId.VERACS_BRASSARD, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.VERACS_BRASSARD).fixedCharges(1000),
            new TriggerItem(ItemId.VERACS_BRASSARD_100),
            new TriggerItem(ItemId.VERACS_BRASSARD_75),
            new TriggerItem(ItemId.VERACS_BRASSARD_50),
            new TriggerItem(ItemId.VERACS_BRASSARD_25),
            new TriggerItem(ItemId.VERACS_BRASSARD_0).fixedCharges(0)
        };
    }
}