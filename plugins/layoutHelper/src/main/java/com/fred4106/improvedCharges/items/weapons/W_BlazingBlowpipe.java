package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class W_BlazingBlowpipe extends W_ToxicBlowpipe {
    public W_BlazingBlowpipe(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.BLAZING_BLOWPIPE, ItemId.BLAZING_BLOWPIPE_UNCHARGED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BLAZING_BLOWPIPE_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.BLAZING_BLOWPIPE),
        };
    }
}
