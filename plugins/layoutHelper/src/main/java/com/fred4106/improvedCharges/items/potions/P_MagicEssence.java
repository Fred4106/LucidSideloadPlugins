package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_MagicEssence extends _Potion {
    public P_MagicEssence(final Provider provider) {
        super("magic_essence", new TriggerItem[]{
            new TriggerItem(ItemId.MAGIC_ESSENCE_1).fixedCharges(1),
            new TriggerItem(ItemId.MAGIC_ESSENCE_2).fixedCharges(2),
            new TriggerItem(ItemId.MAGIC_ESSENCE_3).fixedCharges(3),
            new TriggerItem(ItemId.MAGIC_ESSENCE_4).fixedCharges(4),
        }, provider);
    }
}
