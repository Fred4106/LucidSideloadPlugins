package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_ExtendedSuperAntifireMix extends _Potion {
    public P_ExtendedSuperAntifireMix(final Provider provider) {
        super("extended_super_antifire_mix", new TriggerItem[]{
            new TriggerItem(ItemId.EXTENDED_SUPER_ANTIFIRE_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.EXTENDED_SUPER_ANTIFIRE_MIX_2).fixedCharges(2),
        }, provider);
    }
}
