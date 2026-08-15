package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_SuperRanging extends _Potion {
    public P_SuperRanging(Provider provider) {
        super("super_ranging", new TriggerItem[]{
            new TriggerItem(ItemID.NZONE1DOSE2RANGERSPOTION).fixedCharges(1),
            new TriggerItem(ItemID.NZONE2DOSE2RANGERSPOTION).fixedCharges(2),
            new TriggerItem(ItemID.NZONE3DOSE2RANGERSPOTION).fixedCharges(3),
            new TriggerItem(ItemID.NZONE4DOSE2RANGERSPOTION).fixedCharges(4),
        }, provider);
    }
}
