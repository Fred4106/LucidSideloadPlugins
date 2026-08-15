package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Goading extends _Potion {
    public P_Goading(Provider provider) {
        super("goading", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEGOADING).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEGOADING).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEGOADING).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEGOADING).fixedCharges(4),
        }, provider);
    }
}
