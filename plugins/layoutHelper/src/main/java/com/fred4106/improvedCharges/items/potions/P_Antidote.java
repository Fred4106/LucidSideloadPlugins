package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_Antidote extends _Potion {
    public P_Antidote(final Provider provider) {
        super("antidote", new TriggerItem[]{
            new TriggerItem(ItemId.ANTIDOTE_1).fixedCharges(1),
            new TriggerItem(ItemId.ANTIDOTE_2).fixedCharges(2),
            new TriggerItem(ItemId.ANTIDOTE_3).fixedCharges(3),
            new TriggerItem(ItemId.ANTIDOTE_4).fixedCharges(4),
        }, provider);
    }
}
