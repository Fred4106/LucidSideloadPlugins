package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_SuperFishing extends _Potion {
    public P_SuperFishing(Provider provider) {
        super("super_fishing", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE2FISHERSPOTION).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE2FISHERSPOTION).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE2FISHERSPOTION).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE2FISHERSPOTION).fixedCharges(4),
        }, provider);
    }
}
