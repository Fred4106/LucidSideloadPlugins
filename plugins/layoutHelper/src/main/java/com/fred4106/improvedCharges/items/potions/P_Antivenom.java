package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Antivenom extends _Potion {
    public P_Antivenom(Provider provider) {
        super("antivenom", new TriggerItem[]{
            new TriggerItem(ItemID.ANTIVENOM1).fixedCharges(1),
            new TriggerItem(ItemID.ANTIVENOM2).fixedCharges(2),
            new TriggerItem(ItemID.ANTIVENOM3).fixedCharges(3),
            new TriggerItem(ItemID.ANTIVENOM4).fixedCharges(4),
        }, provider);
    }
}
