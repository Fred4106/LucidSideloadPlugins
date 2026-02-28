package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnVarbitChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.ids.VarbitId;

import java.util.List;

public class U_BowStringSpool extends ChargedItem {
    public U_BowStringSpool(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.BOW_STRING_SPOOL, ItemId.BOW_STRING_SPOOL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BOW_STRING_SPOOL)
        };

        this.triggers.addAll(List.of(
            new OnVarbitChanged(VarbitId.BOW_STRING_SPOOL_CHARGES).setDynamically()
        ));
    }
}
