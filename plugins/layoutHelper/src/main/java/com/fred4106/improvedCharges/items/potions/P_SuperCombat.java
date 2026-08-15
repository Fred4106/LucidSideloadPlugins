package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_SuperCombat extends _Potion {
    public P_SuperCombat(Provider provider) {
        super("super_combat", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE2COMBAT).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE2COMBAT).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE2COMBAT).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE2COMBAT).fixedCharges(4),
        }, provider);
    }
}
