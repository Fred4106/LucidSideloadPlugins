package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_SilkDressing extends _Potion {
    public P_SilkDressing(final Provider provider) {
        super("toa_silk_dressing", new TriggerItem[]{
            new TriggerItem(ItemId.TOA_SILK_DRESSING_1).fixedCharges(1),
            new TriggerItem(ItemId.TOA_SILK_DRESSING_2).fixedCharges(2),
        }, provider);
    }
}
