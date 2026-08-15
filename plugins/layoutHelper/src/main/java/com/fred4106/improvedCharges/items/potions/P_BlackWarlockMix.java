package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_BlackWarlockMix extends _Potion {
    public P_BlackWarlockMix(Provider provider) {
        super("black_warlock_mix", new TriggerItem[]{
            new TriggerItem(ItemID.HUNTER_MIX_WARLOCK_1DOSE).fixedCharges(1),
            new TriggerItem(ItemID.HUNTER_MIX_WARLOCK_2DOSE).fixedCharges(2),
        }, provider);
    }
}
