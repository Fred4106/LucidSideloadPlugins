package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class EclipseMoonTassets extends _MoonItem {
    public EclipseMoonTassets(
        Provider provider
    ) {
        super("Eclipse moon tassets", ItemID.ECLIPSE_MOON_TASSETS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.ECLIPSE_MOON_TASSETS).fixedCharges(3000),
            new TriggerItem(ItemID.ECLIPSE_MOON_TASSETS_DEGRADED),
            new TriggerItem(ItemID.ECLIPSE_MOON_TASSETS_BROKEN).fixedCharges(0),
        };
    }
}