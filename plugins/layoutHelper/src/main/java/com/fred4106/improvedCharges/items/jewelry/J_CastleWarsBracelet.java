package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_CastleWarsBracelet extends ChargedItem {
    public J_CastleWarsBracelet(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.CASTLE_WARS_BRACELET, ItemId.CASTLE_WARS_BRACELET_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CASTLE_WARS_BRACELET_1).fixedCharges(1).needsToBeEquipped(),
            new TriggerItem(ItemId.CASTLE_WARS_BRACELET_2).fixedCharges(2).needsToBeEquipped(),
            new TriggerItem(ItemId.CASTLE_WARS_BRACELET_3).fixedCharges(3).needsToBeEquipped(),
        };
    }
}
