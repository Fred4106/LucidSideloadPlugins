package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_DivineBattlemage extends _Potion {
    public P_DivineBattlemage(Provider provider) {
        super("divine_battlemage", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEDIVINEBATTLEMAGE).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEDIVINEBATTLEMAGE).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEDIVINEBATTLEMAGE).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEDIVINEBATTLEMAGE).fixedCharges(4),
        }, provider);
    }
}
