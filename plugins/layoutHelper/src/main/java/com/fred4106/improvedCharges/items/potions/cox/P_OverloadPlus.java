package com.fred4106.improvedCharges.items.potions.cox;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.items.potions.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_OverloadPlus extends _Potion {
    public P_OverloadPlus(Provider provider) {
        super("cox_overload_plus", new TriggerItem[]{
            new TriggerItem(ItemID.RAIDS_VIAL_OVERLOAD_STRONG_1).fixedCharges(1),
            new TriggerItem(ItemID.RAIDS_VIAL_OVERLOAD_STRONG_2).fixedCharges(2),
            new TriggerItem(ItemID.RAIDS_VIAL_OVERLOAD_STRONG_3).fixedCharges(3),
            new TriggerItem(ItemID.RAIDS_VIAL_OVERLOAD_STRONG_4).fixedCharges(4),
        }, provider);
    }
}
