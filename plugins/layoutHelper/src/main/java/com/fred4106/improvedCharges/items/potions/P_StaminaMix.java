package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_StaminaMix extends _Potion {
    public P_StaminaMix(Provider provider) {
        super("stamina_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSESTAMINA).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSESTAMINA).fixedCharges(2),
        }, provider);
    }
}
