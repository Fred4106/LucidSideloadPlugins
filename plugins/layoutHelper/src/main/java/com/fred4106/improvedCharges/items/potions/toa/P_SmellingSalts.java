package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_SmellingSalts extends _Potion {
    public P_SmellingSalts(final Provider provider) {
        super("toa_smelling_salts", new TriggerItem[]{
            new TriggerItem(ItemId.TOA_SMELLING_SALTS_1).fixedCharges(1),
            new TriggerItem(ItemId.TOA_SMELLING_SALTS_2).fixedCharges(2),
        }, provider);
    }
}
