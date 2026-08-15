package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_DivineBastion extends _Potion {
    public P_DivineBastion(Provider provider) {
        super("divine_bastion", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEDIVINEBASTION).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEDIVINEBASTION).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEDIVINEBASTION).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEDIVINEBASTION).fixedCharges(4),
        }, provider);
    }
}
