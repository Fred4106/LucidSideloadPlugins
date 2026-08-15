package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_FishingMix extends _Potion {
    public P_FishingMix(Provider provider) {
        super("fishing_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSEFISHERSPOTION).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSEFISHERSPOTION).fixedCharges(2),
        }, provider);
    }
}
