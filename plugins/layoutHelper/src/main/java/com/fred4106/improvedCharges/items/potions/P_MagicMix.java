package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_MagicMix extends _Potion {
    public P_MagicMix(Provider provider) {
        super("magic_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSE1MAGIC).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSE1MAGIC).fixedCharges(2),
        }, provider);
    }
}
