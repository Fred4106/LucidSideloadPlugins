package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_ExtendedStamina extends _Potion {
    public P_ExtendedStamina(Provider provider) {
        super("extended_stamina", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSE2STAMINA).fixedCharges(1),
            new TriggerItem(ItemID._2DOSE2STAMINA).fixedCharges(2),
            new TriggerItem(ItemID._3DOSE2STAMINA).fixedCharges(3),
            new TriggerItem(ItemID._4DOSE2STAMINA).fixedCharges(4),
        }, provider);
    }
}
