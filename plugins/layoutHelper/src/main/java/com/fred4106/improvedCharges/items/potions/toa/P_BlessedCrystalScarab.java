package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_BlessedCrystalScarab extends _Potion {
    public P_BlessedCrystalScarab(final Provider provider) {
        super("toa_blessed_crystal_scarab", new TriggerItem[]{
            new TriggerItem(ItemId.TOA_BLESSED_CRYSTAL_SCARAB_1).fixedCharges(1),
            new TriggerItem(ItemId.TOA_BLESSED_CRYSTAL_SCARAB_2).fixedCharges(2),
        }, provider);
    }
}
