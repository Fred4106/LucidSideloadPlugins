package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class J_CombatBracelet extends ChargedItem {
    public J_CombatBracelet(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.COMBAT_BRACELET, ItemId.COMBAT_BRACELET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.COMBAT_BRACELET).fixedCharges(0),
            new TriggerItem(ItemId.COMBAT_BRACELET_1).fixedCharges(1),
            new TriggerItem(ItemId.COMBAT_BRACELET_2).fixedCharges(2),
            new TriggerItem(ItemId.COMBAT_BRACELET_3).fixedCharges(3),
            new TriggerItem(ItemId.COMBAT_BRACELET_4).fixedCharges(4),
            new TriggerItem(ItemId.COMBAT_BRACELET_5).fixedCharges(5),
            new TriggerItem(ItemId.COMBAT_BRACELET_6).fixedCharges(6),
        };
    }
}
