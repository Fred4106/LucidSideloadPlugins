package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_Moonlight extends _Potion {
    public P_Moonlight(Provider provider) {
        super("moonlight", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEMOONLIGHTPOTION).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEMOONLIGHTPOTION).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEMOONLIGHTPOTION).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEMOONLIGHTPOTION).fixedCharges(4),
        }, provider);
    }
}
