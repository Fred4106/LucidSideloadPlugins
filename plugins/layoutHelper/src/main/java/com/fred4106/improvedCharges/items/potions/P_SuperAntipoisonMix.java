package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_SuperAntipoisonMix extends _Potion {
    public P_SuperAntipoisonMix(Provider provider) {
        super("super_antipoison_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSE2ANTIPOISON).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSE2ANTIPOISON).fixedCharges(2),
        }, provider);
    }
}
