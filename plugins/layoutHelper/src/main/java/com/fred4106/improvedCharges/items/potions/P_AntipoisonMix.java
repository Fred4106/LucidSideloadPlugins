package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_AntipoisonMix extends _Potion {
    public P_AntipoisonMix(final Provider provider) {
        super("antipoison_mix", new TriggerItem[]{
            new TriggerItem(ItemId.ANTIPOISON_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.ANTIPOISON_MIX_2).fixedCharges(2),
        }, provider);
    }
}
