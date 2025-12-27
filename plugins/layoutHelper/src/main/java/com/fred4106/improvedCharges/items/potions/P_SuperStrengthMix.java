package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_SuperStrengthMix extends _Potion {
    public P_SuperStrengthMix(final Provider provider) {
        super("super_strength_mix", new TriggerItem[]{
            new TriggerItem(ItemId.SUPER_STRENGTH_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.SUPER_STRENGTH_MIX_2).fixedCharges(2),
        }, provider);
    }
}
