package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class BlueMoonHelm extends _MoonItem {
    public BlueMoonHelm(
        final Provider provider
    ) {
        super("blue_helm", ItemId.BLUE_MOON_HELM, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BLUE_MOON_HELM).fixedCharges(3000),
            new TriggerItem(ItemId.BLUE_MOON_HELM_DEGRADED),
            new TriggerItem(ItemId.BLUE_MOON_HELM_BROKEN).fixedCharges(0),
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("Your Blue moon helm has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // In combat.
            new OnCombat(90).isEquipped().decreaseCharges(1),
        };
    }
}