package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class BlueMoonHelm extends _MoonItem {
    public BlueMoonHelm(
        Provider provider
    ) {
        super("Blue moon helm", ItemID.FROST_MOON_HELM, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.FROST_MOON_HELM).fixedCharges(3000),
            new TriggerItem(ItemID.FROST_MOON_HELM_DEGRADED),
            new TriggerItem(ItemID.FROST_MOON_HELM_BROKEN).fixedCharges(0),
        };
    }
}