package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_PrayerMix extends _Potion {
    public P_PrayerMix(final Provider provider) {
        super("prayer", new TriggerItem[]{
            new TriggerItem(ItemId.PRAYER_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.PRAYER_MIX_2).fixedCharges(2),
        }, provider);
    }
}
