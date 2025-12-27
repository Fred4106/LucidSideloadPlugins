package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class P_MoonlightMothMix extends _Potion {
    public P_MoonlightMothMix(final Provider provider) {
        super("moonlight_moth_mix", new TriggerItem[]{
            new TriggerItem(ItemId.MOONLIGHT_MOTH_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.MOONLIGHT_MOTH_MIX_2).fixedCharges(2),
        }, provider);
    }
}
