package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_ExtendedAntifire extends _Potion {
    public P_ExtendedAntifire(Provider provider) {
        super("extended_antifire", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE2ANTIDRAGON).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE2ANTIDRAGON).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE2ANTIDRAGON).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE2ANTIDRAGON).fixedCharges(4),
        }, provider);
    }
}
