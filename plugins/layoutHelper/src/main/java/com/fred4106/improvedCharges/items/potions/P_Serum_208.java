package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_Serum_208 extends _Potion {
    public P_Serum_208(final Provider provider) {
        super("serum_208", new TriggerItem[]{
            new TriggerItem(ItemId.SERUM_208_1).fixedCharges(1),
            new TriggerItem(ItemId.SERUM_208_2).fixedCharges(2),
            new TriggerItem(ItemId.SERUM_208_3).fixedCharges(3),
            new TriggerItem(ItemId.SERUM_208_4).fixedCharges(4),
        }, provider);
    }
}
