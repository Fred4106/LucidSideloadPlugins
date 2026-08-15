package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_SuperStrength extends _Potion {
    public P_SuperStrength(Provider provider) {
        super("super_strength", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE2STRENGTH).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE2STRENGTH).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE2STRENGTH).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE2STRENGTH).fixedCharges(4),
        }, provider);
    }
}
