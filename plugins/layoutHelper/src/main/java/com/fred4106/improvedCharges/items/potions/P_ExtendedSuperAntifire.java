package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_ExtendedSuperAntifire extends _Potion {
    public P_ExtendedSuperAntifire(Provider provider) {
        super("extended_super_antifire", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE4ANTIDRAGON).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE4ANTIDRAGON).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE4ANTIDRAGON).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE4ANTIDRAGON).fixedCharges(4),
        }, provider);
    }
}
