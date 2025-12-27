package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class U_Ectophial extends ChargedItem {
    public U_Ectophial(Provider provider) {
        super(com.fred4106.improvedCharges.Constants.ECTOPHIAL, ItemId.ECTOPHIAL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ECTOPHIAL_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.ECTOPHIAL).fixedCharges(1),
        };

        this.triggers = new TriggerBase[]{
            // Unify teleport.
            new OnMenuEntryAdded("Empty").replaceOption("Teleport"),
        };
    }
}
