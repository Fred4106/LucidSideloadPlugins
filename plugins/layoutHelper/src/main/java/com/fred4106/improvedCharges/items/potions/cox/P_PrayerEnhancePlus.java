package com.fred4106.improvedCharges.items.potions.cox;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_PrayerEnhancePlus extends _Potion {
    public P_PrayerEnhancePlus(final Provider provider) {
        super("cox_prayer_enhance_plus", new TriggerItem[]{
            new TriggerItem(ItemId.COX_PRAYER_ENHANCE_PLUS_1).fixedCharges(1),
            new TriggerItem(ItemId.COX_PRAYER_ENHANCE_PLUS_2).fixedCharges(2),
            new TriggerItem(ItemId.COX_PRAYER_ENHANCE_PLUS_3).fixedCharges(3),
            new TriggerItem(ItemId.COX_PRAYER_ENHANCE_PLUS_4).fixedCharges(4),
        }, provider);
    }
}
