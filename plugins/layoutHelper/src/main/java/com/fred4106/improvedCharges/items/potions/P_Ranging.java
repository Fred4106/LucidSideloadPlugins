package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_Ranging extends _Potion {
    public P_Ranging(final Provider provider) {
        super("ranging", new TriggerItem[]{
            new TriggerItem(ItemId.RANGING_POTION_1).fixedCharges(1),
            new TriggerItem(ItemId.RANGING_POTION_2).fixedCharges(2),
            new TriggerItem(ItemId.RANGING_POTION_3).fixedCharges(3),
            new TriggerItem(ItemId.RANGING_POTION_4).fixedCharges(4),
        }, provider);
    }
}
