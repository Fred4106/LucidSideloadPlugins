package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class W_WebweaverBow extends W_CrawsBow {
    public W_WebweaverBow(Provider provider) {
        super(FredsItemChargesConfig.webweaver_bow, ItemID.WILD_CAVE_WEBWEAVER_UNCHARGED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.WILD_CAVE_WEBWEAVER_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemID.WILD_CAVE_WEBWEAVER_CHARGED),
        };
    }
}
