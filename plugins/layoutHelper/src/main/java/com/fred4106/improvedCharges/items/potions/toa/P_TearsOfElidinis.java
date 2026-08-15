package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.items.potions.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_TearsOfElidinis extends _Potion {
    public P_TearsOfElidinis(Provider provider) {
        super("toa_tears_of_elidinis", new TriggerItem[]{
            new TriggerItem(ItemID.TOA_SUPPLY_PRAYER_1).fixedCharges(1),
            new TriggerItem(ItemID.TOA_SUPPLY_PRAYER_2).fixedCharges(2),
            new TriggerItem(ItemID.TOA_SUPPLY_PRAYER_3).fixedCharges(3),
            new TriggerItem(ItemID.TOA_SUPPLY_PRAYER_4).fixedCharges(4),
        }, provider);
    }
}
