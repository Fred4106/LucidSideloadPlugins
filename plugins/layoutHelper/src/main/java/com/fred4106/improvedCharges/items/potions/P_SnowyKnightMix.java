package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class P_SnowyKnightMix extends _Potion {
    public P_SnowyKnightMix(final Provider provider) {
        super("snowy_knight_mix", new TriggerItem[]{
            new TriggerItem(ItemId.SNOWY_KNIGHT_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.SNOWY_KNIGHT_MIX_2).fixedCharges(2),
        }, provider);
    }
}
