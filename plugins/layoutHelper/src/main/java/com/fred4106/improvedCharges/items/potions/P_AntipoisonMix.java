package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;
public class P_AntipoisonMix extends _Potion {
    public P_AntipoisonMix(Provider provider) {
        super("antipoison_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSEANTIPOISON).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSEANTIPOISON).fixedCharges(2),
        }, provider);
    }
}
