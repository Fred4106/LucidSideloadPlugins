package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class J_BurningAmulet extends ChargedItem {
    public J_BurningAmulet(
        final Provider provider
    ) {
        super(com.fred4106.improvedCharges.Constants.BURNING_AMULET, ItemId.BURNING_AMULET_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BURNING_AMULET_1).fixedCharges(1),
            new TriggerItem(ItemId.BURNING_AMULET_2).fixedCharges(2),
            new TriggerItem(ItemId.BURNING_AMULET_3).fixedCharges(3),
            new TriggerItem(ItemId.BURNING_AMULET_4).fixedCharges(4),
            new TriggerItem(ItemId.BURNING_AMULET_5).fixedCharges(5),
        };

        this.triggers.addAll(List.of(
            new OnMenuEntryAdded("Rub").replaceOption("Teleport")
        ));
    }
}