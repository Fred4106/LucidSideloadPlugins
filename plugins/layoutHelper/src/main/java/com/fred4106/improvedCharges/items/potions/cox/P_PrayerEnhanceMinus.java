package com.fred4106.improvedCharges.items.potions.cox;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.items.potions.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_PrayerEnhanceMinus extends _Potion {
    public P_PrayerEnhanceMinus(Provider provider) {
        super("cox_prayer_enhance_minus", new TriggerItem[]{
            new TriggerItem(ItemID.RAIDS_VIAL_PRAYER_WEAK_1).fixedCharges(1),
            new TriggerItem(ItemID.RAIDS_VIAL_PRAYER_WEAK_2).fixedCharges(2),
            new TriggerItem(ItemID.RAIDS_VIAL_PRAYER_WEAK_3).fixedCharges(3),
            new TriggerItem(ItemID.RAIDS_VIAL_PRAYER_WEAK_4).fixedCharges(4),
        }, provider);
    }
}
