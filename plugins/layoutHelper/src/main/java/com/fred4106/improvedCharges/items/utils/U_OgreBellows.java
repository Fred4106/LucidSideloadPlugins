package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class U_OgreBellows extends ChargedItem {
    public U_OgreBellows(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.OGRE_BELLOWS, ItemId.OGRE_BELLOWS_0, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.OGRE_BELLOWS_0).fixedCharges(0),
            new TriggerItem(ItemId.OGRE_BELLOWS_1).fixedCharges(1),
            new TriggerItem(ItemId.OGRE_BELLOWS_2).fixedCharges(2),
            new TriggerItem(ItemId.OGRE_BELLOWS_3).fixedCharges(3),
        };
    }
}
