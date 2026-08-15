package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_SuperDefence extends _Potion {
    public P_SuperDefence(Provider provider) {
        super("super_defence", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE2DEFENSE).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE2DEFENSE).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE2DEFENSE).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE2DEFENSE).fixedCharges(4),
        }, provider);
    }
}
