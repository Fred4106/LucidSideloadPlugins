package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class EclipseMoonTassets extends _MoonItem {
    public EclipseMoonTassets(
        final Provider provider
    ) {
        super("eclipse_tassets", ItemId.ECLIPSE_MOON_TASSETS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ECLIPSE_MOON_TASSETS).fixedCharges(3000),
            new TriggerItem(ItemId.ECLIPSE_MOON_TASSETS_DEGRADED),
            new TriggerItem(ItemId.ECLIPSE_MOON_TASSETS_BROKEN).fixedCharges(0),
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("Your Eclipse moon tassets has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // In combat.
            new OnCombat(90).isEquipped().decreaseCharges(1),
        };
    }
}