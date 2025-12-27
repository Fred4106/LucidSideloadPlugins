package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_SuperStrength extends _Potion {
    public P_SuperStrength(final Provider provider) {
        super("super_strength", new TriggerItem[]{
            new TriggerItem(ItemId.SUPER_STRENGTH_1).fixedCharges(1),
            new TriggerItem(ItemId.SUPER_STRENGTH_2).fixedCharges(2),
            new TriggerItem(ItemId.SUPER_STRENGTH_3).fixedCharges(3),
            new TriggerItem(ItemId.SUPER_STRENGTH_4).fixedCharges(4),
        }, provider);
    }
}
