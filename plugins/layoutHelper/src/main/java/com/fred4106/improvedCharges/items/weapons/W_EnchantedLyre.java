package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class W_EnchantedLyre extends ChargedItem {
    public W_EnchantedLyre(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.ENCHANTED_LYRE, ItemId.ENCHANTED_LYRE_0, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ENCHANTED_LYRE_0).fixedCharges(0),
            new TriggerItem(ItemId.ENCHANTED_LYRE_1).fixedCharges(1),
            new TriggerItem(ItemId.ENCHANTED_LYRE_2).fixedCharges(2),
            new TriggerItem(ItemId.ENCHANTED_LYRE_3).fixedCharges(3),
            new TriggerItem(ItemId.ENCHANTED_LYRE_4).fixedCharges(4),
            new TriggerItem(ItemId.ENCHANTED_LYRE_5).fixedCharges(5),
            new TriggerItem(ItemId.ENCHANTED_LYRE_IMBUED).fixedCharges(ChargeId.UNLIMITED),
        };

        this.triggers.addAll(List.of(
            new OnMenuEntryAdded("Play").replaceOption("Teleport")
        ));
    }
}
