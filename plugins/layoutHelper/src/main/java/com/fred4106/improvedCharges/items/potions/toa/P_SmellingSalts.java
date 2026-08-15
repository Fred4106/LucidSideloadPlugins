package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.items.potions.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_SmellingSalts extends _Potion {
    public P_SmellingSalts(Provider provider) {
        super("toa_smelling_salts", new TriggerItem[]{
            new TriggerItem(ItemID.TOA_SUPPLY_STATS_1).fixedCharges(1),
            new TriggerItem(ItemID.TOA_SUPPLY_STATS_2).fixedCharges(2),
        }, provider);
    }
}
