package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class U_Ectophial extends ChargedItem {
    public U_Ectophial(Provider provider) {
        super(FredsItemChargesConfig.ectophial, ItemID.ECTOPHIAL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.ECTOPHIAL_EMPTY).fixedCharges(0),
            new TriggerItem(ItemID.ECTOPHIAL).fixedCharges(1),
        };

        this.triggers.addAll(List.of(
            // Unify teleport.
            new OnMenuEntryAdded("Empty").replaceOption("Teleport")
        ));
    }
}
