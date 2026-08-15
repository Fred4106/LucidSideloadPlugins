package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStatus;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.Provider;

public class U_ImpInABox extends ChargedItemWithStatus {
    public U_ImpInABox(Provider provider) {
        super(FredsItemChargesConfig.imp_in_a_box, ItemID.MAGIC_IMP_BOX_HALF, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.MAGIC_IMP_BOX_HALF).fixedCharges(1),
            new TriggerItem(ItemID.MAGIC_IMP_BOX_FULL).fixedCharges(2),
        };
    }
}
