package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Energy extends _Potion {
    public P_Energy(Provider provider) {
        super("energy", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE1ENERGY).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE1ENERGY).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE1ENERGY).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE1ENERGY).fixedCharges(4),
        }, provider);
    }
}
