package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_HuntingMix extends _Potion {
    public P_HuntingMix(Provider provider) {
        super("hunting_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSE1HUNTING).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSE1HUNTING).fixedCharges(2),
        }, provider);
    }
}
