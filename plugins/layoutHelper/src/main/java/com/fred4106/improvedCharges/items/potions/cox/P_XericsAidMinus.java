package com.fred4106.improvedCharges.items.potions.cox;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class P_XericsAidMinus extends _Potion {
    public P_XericsAidMinus(final Provider provider) {
        super("cox_xerics_aid_minus", new TriggerItem[]{
            new TriggerItem(ItemId.COX_XERICS_AID_MINUS_1).fixedCharges(1),
            new TriggerItem(ItemId.COX_XERICS_AID_MINUS_2).fixedCharges(2),
            new TriggerItem(ItemId.COX_XERICS_AID_MINUS_3).fixedCharges(3),
            new TriggerItem(ItemId.COX_XERICS_AID_MINUS_4).fixedCharges(4),
        }, provider);
    }
}
