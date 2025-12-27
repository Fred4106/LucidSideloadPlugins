package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_Nectar extends _Potion {
    public P_Nectar(final Provider provider) {
        super("toa_nectar", new TriggerItem[]{
            new TriggerItem(ItemId.TOA_NECTAR_1).fixedCharges(1),
            new TriggerItem(ItemId.TOA_NECTAR_2).fixedCharges(2),
            new TriggerItem(ItemId.TOA_NECTAR_3).fixedCharges(3),
            new TriggerItem(ItemId.TOA_NECTAR_4).fixedCharges(4),
        }, provider);
    }
}
