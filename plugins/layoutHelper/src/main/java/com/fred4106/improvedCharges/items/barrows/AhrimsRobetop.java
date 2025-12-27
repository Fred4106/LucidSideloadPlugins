package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class AhrimsRobetop extends _BarrowsItem {
    public AhrimsRobetop(final Provider provider) {
        super("Ahrim's body", ItemId.AHRIMS_ROBETOP, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.AHRIMS_ROBETOP).fixedCharges(1000),
            new TriggerItem(ItemId.AHRIMS_ROBETOP_100),
            new TriggerItem(ItemId.AHRIMS_ROBETOP_75),
            new TriggerItem(ItemId.AHRIMS_ROBETOP_50),
            new TriggerItem(ItemId.AHRIMS_ROBETOP_25),
            new TriggerItem(ItemId.AHRIMS_ROBETOP_0).fixedCharges(0),
        };
    }
}