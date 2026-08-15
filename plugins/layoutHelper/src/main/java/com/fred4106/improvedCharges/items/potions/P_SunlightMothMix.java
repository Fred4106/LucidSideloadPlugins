package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_SunlightMothMix extends _Potion {
    public P_SunlightMothMix(Provider provider) {
        super("sunlight_moth_mix", new TriggerItem[]{
            new TriggerItem(ItemID.HUNTER_MIX_SUNMOTH_1DOSE).fixedCharges(1),
            new TriggerItem(ItemID.HUNTER_MIX_SUNMOTH_2DOSE).fixedCharges(2),
        }, provider);
    }

    @Override
    public String getTooltip() {
        return "Sunlight moth mix: " + this.getTotalChargesString();
    }
}
