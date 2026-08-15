package com.fred4106.improvedCharges.items.potions.toa;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.items.potions._Potion;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.items.potions.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_LiquidAdrenaline extends _Potion {
    public P_LiquidAdrenaline(Provider provider) {
        super("toa_liquid_adrenaline", new TriggerItem[]{
            new TriggerItem(ItemID.TOA_SUPPLY_ENERGY_1).fixedCharges(1),
            new TriggerItem(ItemID.TOA_SUPPLY_ENERGY_2).fixedCharges(2),
        }, provider);
    }
}
