package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Battlemage extends _Potion {
    public P_Battlemage(Provider provider) {
        super("battlemage", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEBATTLEMAGE).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEBATTLEMAGE).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEBATTLEMAGE).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEBATTLEMAGE).fixedCharges(4),
        }, provider);
    }
}
