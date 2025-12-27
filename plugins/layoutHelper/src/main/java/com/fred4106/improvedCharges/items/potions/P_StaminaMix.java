package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_StaminaMix extends _Potion {
    public P_StaminaMix(final Provider provider) {
        super("stamina_mix", new TriggerItem[]{
            new TriggerItem(ItemId.STAMINA_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.STAMINA_MIX_2).fixedCharges(2),
        }, provider);
    }
}
