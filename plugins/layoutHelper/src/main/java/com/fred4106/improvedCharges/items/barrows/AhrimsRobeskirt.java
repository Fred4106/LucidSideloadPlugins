package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class AhrimsRobeskirt extends _BarrowsItem {
    public AhrimsRobeskirt(final Provider provider) {
        super("Ahrim's skirt", ItemId.AHRIMS_ROBESKIRT, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.AHRIMS_ROBESKIRT).fixedCharges(1000),
            new TriggerItem(ItemId.AHRIMS_ROBESKIRT_100),
            new TriggerItem(ItemId.AHRIMS_ROBESKIRT_75),
            new TriggerItem(ItemId.AHRIMS_ROBESKIRT_50),
            new TriggerItem(ItemId.AHRIMS_ROBESKIRT_25),
            new TriggerItem(ItemId.AHRIMS_ROBESKIRT_0).fixedCharges(0),
        };
    }
}