package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_PrayerMix extends _Potion {
    public P_PrayerMix(Provider provider) {
        super("prayer", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSEPRAYERRESTORE).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSEPRAYERRESTORE).fixedCharges(2),
        }, provider);
    }
}
