package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class U_Waterskin extends ChargedItem {
    public U_Waterskin(Provider provider) {
        super(FredsItemChargesConfig.waterskin, ItemID.WATER_SKIN0, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.WATER_SKIN0).fixedCharges(0),
            new TriggerItem(ItemID.WATER_SKIN1).fixedCharges(1),
            new TriggerItem(ItemID.WATER_SKIN2).fixedCharges(2),
            new TriggerItem(ItemID.WATER_SKIN3).fixedCharges(3),
            new TriggerItem(ItemID.WATER_SKIN4).fixedCharges(4),
        };
    }
}
