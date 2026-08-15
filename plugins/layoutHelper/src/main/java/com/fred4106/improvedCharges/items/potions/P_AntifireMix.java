package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_AntifireMix extends _Potion {
    public P_AntifireMix(Provider provider) {
        super("antifire_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSE1ANTIDRAGON).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSE1ANTIDRAGON).fixedCharges(2),
        }, provider);
    }
}
