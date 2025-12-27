package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class BloodMoonHelm extends _MoonItem {
    public BloodMoonHelm(
        final Provider provider
    ) {
        super("blood_helm", ItemId.BLOOD_MOON_HELM, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BLOOD_MOON_HELM).fixedCharges(3000),
            new TriggerItem(ItemId.BLOOD_MOON_HELM_DEGRADED),
            new TriggerItem(ItemId.BLOOD_MOON_HELM_BROKEN).fixedCharges(0),
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("Your Blood moon helm has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // In combat.
            new OnCombat(90).isEquipped().decreaseCharges(1),
        };
    }
}