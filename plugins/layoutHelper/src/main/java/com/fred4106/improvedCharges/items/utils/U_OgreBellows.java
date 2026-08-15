package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class U_OgreBellows extends ChargedItem {
    public U_OgreBellows(Provider provider) {
        super(FredsItemChargesConfig.ogre_bellows, ItemID.EMPTY_OGRE_BELLOWS, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.EMPTY_OGRE_BELLOWS).fixedCharges(0),
            new TriggerItem(ItemID.FILLED_OGRE_BELLOW1).fixedCharges(1),
            new TriggerItem(ItemID.FILLED_OGRE_BELLOW2).fixedCharges(2),
            new TriggerItem(ItemID.FILLED_OGRE_BELLOW3).fixedCharges(3),
        };
    }
}
