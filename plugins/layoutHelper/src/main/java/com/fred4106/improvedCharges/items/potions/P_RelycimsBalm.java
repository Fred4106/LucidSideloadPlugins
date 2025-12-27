package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_RelycimsBalm extends _Potion {
    public P_RelycimsBalm(final Provider provider) {
        super("relicyms_balm", new TriggerItem[]{
            new TriggerItem(ItemId.RELICYMS_BALM_1).fixedCharges(1),
            new TriggerItem(ItemId.RELICYMS_BALM_2).fixedCharges(2),
            new TriggerItem(ItemId.RELICYMS_BALM_3).fixedCharges(3),
            new TriggerItem(ItemId.RELICYMS_BALM_4).fixedCharges(4),
        }, provider);
    }
}
