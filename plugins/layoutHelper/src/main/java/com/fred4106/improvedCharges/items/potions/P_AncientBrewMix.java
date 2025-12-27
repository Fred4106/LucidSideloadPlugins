package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_AncientBrewMix extends _Potion {
    public P_AncientBrewMix(final Provider provider) {
        super("ancient_brew_mix", new TriggerItem[]{
            new TriggerItem(ItemId.ANCIENT_BREW_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.ANCIENT_BREW_MIX_2).fixedCharges(2),
        }, provider);
    }
}
