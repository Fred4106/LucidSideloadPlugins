package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class BlueMoonChestplate extends _MoonItem {
    public BlueMoonChestplate(
        final Provider provider
    ) {
        super("blue_chestplate", ItemId.BLUE_MOON_CHESTPLATE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BLUE_MOON_CHESTPLATE).fixedCharges(3000),
            new TriggerItem(ItemId.BLUE_MOON_CHESTPLATE_DEGRADED),
            new TriggerItem(ItemId.BLUE_MOON_CHESTPLATE_BROKEN).fixedCharges(0),
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("Your Blue moon chestplate has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // In combat.
            new OnCombat(90).isEquipped().decreaseCharges(1),
        };
    }
}