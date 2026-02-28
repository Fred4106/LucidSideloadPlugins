package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_HaemostaticDressing extends _Potion{
    public P_HaemostaticDressing(final Provider provider){
        super("haemostatic_dressing", new TriggerItem[]{
                new TriggerItem(ItemId.HAEMOSTATIC_DRESSING_1).fixedCharges(1),
                new TriggerItem(ItemId.HAEMOSTATIC_DRESSING_2).fixedCharges(2),
                new TriggerItem(ItemId.HAEMOSTATIC_DRESSING_3).fixedCharges(3),
                new TriggerItem(ItemId.HAEMOSTATIC_DRESSING_4).fixedCharges(4),
        }, provider);
    }
}
