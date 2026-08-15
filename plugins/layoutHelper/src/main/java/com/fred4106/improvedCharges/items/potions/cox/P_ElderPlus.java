package com.fred4106.improvedCharges.items.potions.cox;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.items.potions.*;
import com.fred4106.improvedCharges.store.*;

public class P_ElderPlus extends _Potion {
    public P_ElderPlus(Provider provider) {
        super("cox_elder_plus", new TriggerItem[]{
            new TriggerItem(ItemID.RAIDS_VIAL_ELDER_STRONG_1).fixedCharges(1),
            new TriggerItem(ItemID.RAIDS_VIAL_ELDER_STRONG_2).fixedCharges(2),
            new TriggerItem(ItemID.RAIDS_VIAL_ELDER_STRONG_3).fixedCharges(3),
            new TriggerItem(ItemID.RAIDS_VIAL_ELDER_STRONG_4).fixedCharges(4),
        }, provider);
    }
}
