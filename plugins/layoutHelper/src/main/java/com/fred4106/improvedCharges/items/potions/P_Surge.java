package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class P_Surge extends _Potion {
    public P_Surge(final Provider provider) {
        super("surge", new TriggerItem[]{
            new TriggerItem(ItemId.SURGE_POTION_1).fixedCharges(1),
            new TriggerItem(ItemId.SURGE_POTION_2).fixedCharges(2),
            new TriggerItem(ItemId.SURGE_POTION_3).fixedCharges(3),
            new TriggerItem(ItemId.SURGE_POTION_4).fixedCharges(4),
        }, provider);
    }
}
