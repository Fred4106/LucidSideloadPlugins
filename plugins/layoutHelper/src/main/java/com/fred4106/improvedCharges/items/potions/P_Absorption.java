package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Absorption extends _Potion {
    public P_Absorption(Provider provider) {
        super("absorption", new TriggerItem[]{
            new TriggerItem(ItemID.NZONE1DOSEABSORPTIONPOTION).fixedCharges(1),
            new TriggerItem(ItemID.NZONE2DOSEABSORPTIONPOTION).fixedCharges(2),
            new TriggerItem(ItemID.NZONE3DOSEABSORPTIONPOTION).fixedCharges(3),
            new TriggerItem(ItemID.NZONE4DOSEABSORPTIONPOTION).fixedCharges(4),
        }, provider);
    }
}
