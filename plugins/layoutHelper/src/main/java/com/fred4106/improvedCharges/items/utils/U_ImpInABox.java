package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItemWithStatus;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class U_ImpInABox extends ChargedItemWithStatus {
    public U_ImpInABox(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.IMP_IN_A_BOX, ItemId.IMP_IN_A_BOX_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.IMP_IN_A_BOX_1).fixedCharges(1),
            new TriggerItem(ItemId.IMP_IN_A_BOX_2).fixedCharges(2),
        };
    }
}
