package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_DivineRanging extends _Potion {
    public P_DivineRanging(Provider provider) {
        super("divine_ranging", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEDIVINERANGE).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEDIVINERANGE).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEDIVINERANGE).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEDIVINERANGE).fixedCharges(4),
        }, provider);
    }
}
