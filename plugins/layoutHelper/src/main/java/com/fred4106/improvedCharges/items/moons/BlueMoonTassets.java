package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class BlueMoonTassets extends _MoonItem {
    public BlueMoonTassets(
        final Provider provider
    ) {
        super("blue_tassets", ItemId.BLUE_MOON_TASSETS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BLUE_MOON_TASSETS).fixedCharges(3000),
            new TriggerItem(ItemId.BLUE_MOON_TASSETS_DEGRADED),
            new TriggerItem(ItemId.BLUE_MOON_TASSETS_BROKEN).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your Blue moon tassets has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // In combat.
            new OnCombat(90).isEquipped().decreaseCharges(1)
        ));
    }
}