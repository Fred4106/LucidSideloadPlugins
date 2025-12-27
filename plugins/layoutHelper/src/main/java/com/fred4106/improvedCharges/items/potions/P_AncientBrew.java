package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_AncientBrew extends _Potion {
    public P_AncientBrew(final Provider provider) {
        super("ancient_brew", new TriggerItem[]{
            new TriggerItem(ItemId.ANCIENT_BREW_1).fixedCharges(1),
            new TriggerItem(ItemId.ANCIENT_BREW_2).fixedCharges(2),
            new TriggerItem(ItemId.ANCIENT_BREW_3).fixedCharges(3),
            new TriggerItem(ItemId.ANCIENT_BREW_4).fixedCharges(4),
        }, provider);
    }
}
