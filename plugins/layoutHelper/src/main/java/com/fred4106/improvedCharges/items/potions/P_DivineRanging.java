package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_DivineRanging extends _Potion {
    public P_DivineRanging(final Provider provider) {
        super("divine_ranging", new TriggerItem[]{
            new TriggerItem(ItemId.DIVINE_RANGING_POTION_1).fixedCharges(1),
            new TriggerItem(ItemId.DIVINE_RANGING_POTION_2).fixedCharges(2),
            new TriggerItem(ItemId.DIVINE_RANGING_POTION_3).fixedCharges(3),
            new TriggerItem(ItemId.DIVINE_RANGING_POTION_4).fixedCharges(4),
        }, provider);
    }
}
