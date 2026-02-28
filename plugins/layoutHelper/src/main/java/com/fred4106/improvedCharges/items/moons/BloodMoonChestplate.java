package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class BloodMoonChestplate extends _MoonItem {
    public BloodMoonChestplate(
        final Provider provider
    ) {
        super("blood_chestplate", ItemId.BLOOD_MOON_CHESTPLATE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BLOOD_MOON_CHESTPLATE).fixedCharges(3000),
            new TriggerItem(ItemId.BLOOD_MOON_CHESTPLATE_DEGRADED),
            new TriggerItem(ItemId.BLOOD_MOON_CHESTPLATE_BROKEN).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your Blood moon chestplate has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // In combat.
            new OnCombat(90).isEquipped().decreaseCharges(1)
        ));
    }
}