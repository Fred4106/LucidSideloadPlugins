package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_Absorption extends _Potion {
    public P_Absorption(final Provider provider) {
        super("absorption", new TriggerItem[]{
            new TriggerItem(ItemId.ABSORPTION_1).fixedCharges(1),
            new TriggerItem(ItemId.ABSORPTION_2).fixedCharges(2),
            new TriggerItem(ItemId.ABSORPTION_3).fixedCharges(3),
            new TriggerItem(ItemId.ABSORPTION_4).fixedCharges(4),
        }, provider);
    }
}
