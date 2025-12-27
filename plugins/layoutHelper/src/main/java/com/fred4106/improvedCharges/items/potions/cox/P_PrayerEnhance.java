package com.fred4106.improvedCharges.items.potions.cox;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_PrayerEnhance extends _Potion {
    public P_PrayerEnhance(final Provider provider) {
        super("cox_prayer_enhance", new TriggerItem[]{
            new TriggerItem(ItemId.COX_PRAYER_ENHANCE_1).fixedCharges(1),
            new TriggerItem(ItemId.COX_PRAYER_ENHANCE_2).fixedCharges(2),
            new TriggerItem(ItemId.COX_PRAYER_ENHANCE_3).fixedCharges(3),
            new TriggerItem(ItemId.COX_PRAYER_ENHANCE_4).fixedCharges(4),
        }, provider);
    }
}
