package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Ranging extends _Potion {
    public P_Ranging(Provider provider) {
        super("ranging", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSERANGERSPOTION).fixedCharges(1),
            new TriggerItem(ItemID._2DOSERANGERSPOTION).fixedCharges(2),
            new TriggerItem(ItemID._3DOSERANGERSPOTION).fixedCharges(3),
            new TriggerItem(ItemID._4DOSERANGERSPOTION).fixedCharges(4),
        }, provider);
    }
}
