package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_RestoreMix extends _Potion {
    public P_RestoreMix(final Provider provider) {
        super("restore_mix", new TriggerItem[]{
            new TriggerItem(ItemId.RESTORE_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.RESTORE_MIX_2).fixedCharges(2),
        }, provider);
    }
}
