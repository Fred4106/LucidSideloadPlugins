package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_SuperStrengthMix extends _Potion {
    public P_SuperStrengthMix(Provider provider) {
        super("super_strength_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSE2STRENGTH).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSE2STRENGTH).fixedCharges(2),
        }, provider);
    }
}
