package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class BlueMoonChestplate extends _MoonItem {
    public BlueMoonChestplate(
        Provider provider
    ) {
        super("Blue moon chestplate", ItemID.FROST_MOON_CHESTPLATE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.FROST_MOON_CHESTPLATE).fixedCharges(3000),
            new TriggerItem(ItemID.FROST_MOON_CHESTPLATE_DEGRADED),
            new TriggerItem(ItemID.FROST_MOON_CHESTPLATE_BROKEN).fixedCharges(0),
        };
    }
}