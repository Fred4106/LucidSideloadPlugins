package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_SuperHunting extends _Potion {
    public P_SuperHunting(Provider provider) {
        super("super_hunting", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE2HUNTING).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE2HUNTING).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE2HUNTING).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE2HUNTING).fixedCharges(4),
        }, provider);
    }
}
