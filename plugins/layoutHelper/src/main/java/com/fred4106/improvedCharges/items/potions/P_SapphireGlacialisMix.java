package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class P_SapphireGlacialisMix extends _Potion {
    public P_SapphireGlacialisMix(final Provider provider) {
        super("sapphire_glacialis_mix", new TriggerItem[]{
            new TriggerItem(ItemId.SAPPHIRE_GLACIALIS_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.SAPPHIRE_GLACIALIS_MIX_2).fixedCharges(2),
        }, provider);
    }
}
