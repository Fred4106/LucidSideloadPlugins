package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.Provider;

public class U_EternalTeleportCrystal extends ChargedItem {
    public U_EternalTeleportCrystal(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.ETERNAL_TELEPORT_CRYSTAL, ItemId.ETERNAL_TELEPORT_CRYSTAL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ETERNAL_TELEPORT_CRYSTAL).fixedCharges(ChargeId.UNLIMITED),
        };
    }
}
