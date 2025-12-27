package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class W_WebweaverBow extends W_CrawsBow {
    public W_WebweaverBow(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.WEBWEAVER_BOW, ItemId.WEBWEAVER_BOW_UNCHARGED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.WEBWEAVER_BOW_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.WEBWEAVER_BOW),
        };
    }
}
