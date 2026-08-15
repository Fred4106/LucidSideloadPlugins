package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_MoonlightMothMix extends _Potion {
    public P_MoonlightMothMix(Provider provider) {
        super("moonlight_moth_mix", new TriggerItem[]{
            new TriggerItem(ItemID.HUNTER_MIX_MOONMOTH_1DOSE).fixedCharges(1),
            new TriggerItem(ItemID.HUNTER_MIX_MOONMOTH_2DOSE).fixedCharges(2),
        }, provider);
    }
}
