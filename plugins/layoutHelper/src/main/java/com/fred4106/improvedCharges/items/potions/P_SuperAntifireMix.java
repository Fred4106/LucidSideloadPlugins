package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_SuperAntifireMix extends _Potion {
    public P_SuperAntifireMix(final Provider provider) {
        super("super_antifire_mix", new TriggerItem[]{
            new TriggerItem(ItemId.SUPER_ANTIFIRE_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.SUPER_ANTIFIRE_MIX_2).fixedCharges(2),
        }, provider);
    }
}
