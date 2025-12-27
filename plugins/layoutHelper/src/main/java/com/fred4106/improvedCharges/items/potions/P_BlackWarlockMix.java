package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class P_BlackWarlockMix extends _Potion {
    public P_BlackWarlockMix(final Provider provider) {
        super("black_warlock_mix", new TriggerItem[]{
            new TriggerItem(ItemId.BLACK_WARLOCK_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.BLACK_WARLOCK_MIX_2).fixedCharges(2),
        }, provider);
    }
}
