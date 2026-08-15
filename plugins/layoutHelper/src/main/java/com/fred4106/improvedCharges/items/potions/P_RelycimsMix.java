package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_RelycimsMix extends _Potion {
    public P_RelycimsMix(Provider provider) {
        super("relicyms_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_RELICYMS_BALM1).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_RELICYMS_BALM2).fixedCharges(2),
        }, provider);
    }
}
