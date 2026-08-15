package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.items.potions.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Ambrosia extends _Potion {
    public P_Ambrosia(Provider provider) {
        super("toa_ambrosia", new TriggerItem[]{
            new TriggerItem(ItemID.TOA_SUPPLY_PANICHEAL_1).fixedCharges(1),
            new TriggerItem(ItemID.TOA_SUPPLY_PANICHEAL_2).fixedCharges(2),
        }, provider);
    }
}
