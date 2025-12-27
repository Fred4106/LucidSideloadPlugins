package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_FishingMix extends _Potion {
    public P_FishingMix(final Provider provider) {
        super("fishing_mix", new TriggerItem[]{
            new TriggerItem(ItemId.FISHING_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.FISHING_MIX_2).fixedCharges(2),
        }, provider);
    }
}
