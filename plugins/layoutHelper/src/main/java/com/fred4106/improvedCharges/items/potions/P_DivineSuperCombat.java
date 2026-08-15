package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_DivineSuperCombat extends _Potion {
    public P_DivineSuperCombat(Provider provider) {
        super("divine_super_combat", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEDIVINECOMBAT).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEDIVINECOMBAT).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEDIVINECOMBAT).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEDIVINECOMBAT).fixedCharges(4),
        }, provider);
    }
}
