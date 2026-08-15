package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_DivineMagic extends _Potion {
    public P_DivineMagic(Provider provider) {
        super("divine_magic", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEDIVINEMAGIC).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEDIVINEMAGIC).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEDIVINEMAGIC).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEDIVINEMAGIC).fixedCharges(4),
        }, provider);
    }
}
