package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_SuperAntipoison extends _Potion {
    public P_SuperAntipoison(Provider provider) {
        super("super_antipoison", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE2ANTIPOISON).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE2ANTIPOISON).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE2ANTIPOISON).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE2ANTIPOISON).fixedCharges(4),
        }, provider);
    }
}
