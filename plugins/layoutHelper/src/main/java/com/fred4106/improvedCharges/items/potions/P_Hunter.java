package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Hunter extends _Potion {
    public P_Hunter(Provider provider) {
        super("hunter", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEHUNTING).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEHUNTING).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEHUNTING).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEHUNTING).fixedCharges(4),
        }, provider);
    }
}
