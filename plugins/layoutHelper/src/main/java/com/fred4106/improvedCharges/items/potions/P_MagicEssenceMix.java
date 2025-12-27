package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_MagicEssenceMix extends _Potion {
    public P_MagicEssenceMix(final Provider provider) {
        super("magic_essence_mix", new TriggerItem[]{
            new TriggerItem(ItemId.MAGIC_ESSENCE_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.MAGIC_ESSENCE_MIX_2).fixedCharges(2),
        }, provider);
    }
}
