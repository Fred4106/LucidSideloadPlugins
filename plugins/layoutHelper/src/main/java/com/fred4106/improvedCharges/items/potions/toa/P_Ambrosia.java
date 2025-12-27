package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_Ambrosia extends _Potion {
    public P_Ambrosia(final Provider provider) {
        super("toa_ambrosia", new TriggerItem[]{
            new TriggerItem(ItemId.TOA_AMBROSIA_1).fixedCharges(1),
            new TriggerItem(ItemId.TOA_AMBROSIA_2).fixedCharges(2),
        }, provider);
    }
}
