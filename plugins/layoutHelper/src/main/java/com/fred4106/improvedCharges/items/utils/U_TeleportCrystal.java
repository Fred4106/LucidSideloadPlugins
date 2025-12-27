package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class U_TeleportCrystal extends ChargedItem {
    public U_TeleportCrystal(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.TELEPORT_CRYSTAL, ItemId.TELEPORT_CRYSTAL_0, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TELEPORT_CRYSTAL_0).fixedCharges(0),
            new TriggerItem(ItemId.TELEPORT_CRYSTAL_1).fixedCharges(1),
            new TriggerItem(ItemId.TELEPORT_CRYSTAL_2).fixedCharges(2),
            new TriggerItem(ItemId.TELEPORT_CRYSTAL_3).fixedCharges(3),
            new TriggerItem(ItemId.TELEPORT_CRYSTAL_4).fixedCharges(4),
            new TriggerItem(ItemId.TELEPORT_CRYSTAL_5).fixedCharges(5),
        };
    }
}
