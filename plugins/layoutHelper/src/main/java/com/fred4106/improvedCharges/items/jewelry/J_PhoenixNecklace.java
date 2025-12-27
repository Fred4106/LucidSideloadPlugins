package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_PhoenixNecklace extends ChargedItem {
    public J_PhoenixNecklace(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.PHOENIX_NECKLACE, ItemId.PHOENIX_NECKLACE, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.PHOENIX_NECKLACE).fixedCharges(1).needsToBeEquipped(),
        };
    }
}
