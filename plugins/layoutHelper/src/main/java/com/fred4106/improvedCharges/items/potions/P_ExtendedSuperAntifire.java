package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_ExtendedSuperAntifire extends _Potion {
    public P_ExtendedSuperAntifire(final Provider provider) {
        super("extended_super_antifire", new TriggerItem[]{
            new TriggerItem(ItemId.EXTENDED_SUPER_ANTIFIRE_1).fixedCharges(1),
            new TriggerItem(ItemId.EXTENDED_SUPER_ANTIFIRE_2).fixedCharges(2),
            new TriggerItem(ItemId.EXTENDED_SUPER_ANTIFIRE_3).fixedCharges(3),
            new TriggerItem(ItemId.EXTENDED_SUPER_ANTIFIRE_4).fixedCharges(4),
        }, provider);
    }
}
