package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_MenaphiteRemedy extends _Potion {
    public P_MenaphiteRemedy(final Provider provider) {
        super("menaphite_remedy", new TriggerItem[]{
            new TriggerItem(ItemId.MENAPHITE_REMEDY_1).fixedCharges(1),
            new TriggerItem(ItemId.MENAPHITE_REMEDY_2).fixedCharges(2),
            new TriggerItem(ItemId.MENAPHITE_REMEDY_3).fixedCharges(3),
            new TriggerItem(ItemId.MENAPHITE_REMEDY_4).fixedCharges(4),
        }, provider);
    }
}
