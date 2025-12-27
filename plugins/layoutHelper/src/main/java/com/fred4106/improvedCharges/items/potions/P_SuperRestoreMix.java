package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_SuperRestoreMix extends _Potion {
    public P_SuperRestoreMix(final Provider provider) {
        super("super_restore_mix", new TriggerItem[]{
            new TriggerItem(ItemId.SUPER_RESTORE_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.SUPER_RESTORE_MIX_2).fixedCharges(2),
        }, provider);
    }
}
