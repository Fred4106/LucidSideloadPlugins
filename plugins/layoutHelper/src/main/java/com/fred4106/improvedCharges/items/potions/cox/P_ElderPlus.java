package com.fred4106.improvedCharges.items.potions.cox;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_ElderPlus extends _Potion {
    public P_ElderPlus(final Provider provider) {
        super("cox_elder_plus", new TriggerItem[]{
            new TriggerItem(ItemId.COX_ELDER_PLUS_1).fixedCharges(1),
            new TriggerItem(ItemId.COX_ELDER_PLUS_2).fixedCharges(2),
            new TriggerItem(ItemId.COX_ELDER_PLUS_3).fixedCharges(3),
            new TriggerItem(ItemId.COX_ELDER_PLUS_4).fixedCharges(4),
        }, provider);
    }
}
