package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_DivineSuperStrength extends _Potion {
    public P_DivineSuperStrength(Provider provider) {
        super("divine_super_strength", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEDIVINESTRENGTH).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEDIVINESTRENGTH).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEDIVINESTRENGTH).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEDIVINESTRENGTH).fixedCharges(4),
        }, provider);
    }
}
