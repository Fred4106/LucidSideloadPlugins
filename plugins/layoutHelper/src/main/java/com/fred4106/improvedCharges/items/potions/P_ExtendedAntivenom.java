package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_ExtendedAntivenom extends _Potion {
    public P_ExtendedAntivenom(Provider provider) {
        super("extended_antivenom", new TriggerItem[]{
            new TriggerItem(ItemID.EXTENDED_ANTIVENOM_1).fixedCharges(1),
            new TriggerItem(ItemID.EXTENDED_ANTIVENOM_2).fixedCharges(2),
            new TriggerItem(ItemID.EXTENDED_ANTIVENOM_3).fixedCharges(3),
            new TriggerItem(ItemID.EXTENDED_ANTIVENOM_4).fixedCharges(4),
        }, provider);
    }
}
