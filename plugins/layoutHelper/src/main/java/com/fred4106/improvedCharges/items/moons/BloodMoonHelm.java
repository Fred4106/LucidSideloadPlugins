package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class BloodMoonHelm extends _MoonItem {
    public BloodMoonHelm(
        Provider provider
    ) {
        super("Blood moon helm", ItemID.BLOOD_MOON_HELM, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BLOOD_MOON_HELM).fixedCharges(3000),
            new TriggerItem(ItemID.BLOOD_MOON_HELM_DEGRADED),
            new TriggerItem(ItemID.BLOOD_MOON_HELM_BROKEN).fixedCharges(0),
        };
    }
}