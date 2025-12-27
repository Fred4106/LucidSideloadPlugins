package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class P_SunlightMothMix extends _Potion {
    public P_SunlightMothMix(final Provider provider) {
        super("sunlight_moth_mix", new TriggerItem[]{
            new TriggerItem(ItemId.SUNLIGHT_MOTH_MIX_1).fixedCharges(1),
            new TriggerItem(ItemId.SUNLIGHT_MOTH_MIX_2).fixedCharges(2),
        }, provider);
    }

    @Override
    public String getTooltip() {
        return "Sunlight moth mix: " + this.getTotalChargesString();
    }
}
