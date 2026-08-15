package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_GuthixBalance extends _Potion {
    public P_GuthixBalance(Provider provider) {
        super("guthix_balance", new TriggerItem[]{
            new TriggerItem(ItemID.BURGH_GUTHIX_BALANCE_1).fixedCharges(1),
            new TriggerItem(ItemID.BURGH_GUTHIX_BALANCE_2).fixedCharges(2),
            new TriggerItem(ItemID.BURGH_GUTHIX_BALANCE_3).fixedCharges(3),
            new TriggerItem(ItemID.BURGH_GUTHIX_BALANCE_4).fixedCharges(4),
        }, provider);
    }
}
