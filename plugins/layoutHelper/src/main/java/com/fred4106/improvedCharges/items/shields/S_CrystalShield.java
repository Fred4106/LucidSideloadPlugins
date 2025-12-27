package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import com.fred4106.improvedCharges.store.Provider;

public class S_CrystalShield extends ChargedItem {
    public S_CrystalShield(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.CRYSTAL_SHIELD, ItemId.CRYSTAL_SHIELD_DEGRADED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CRYSTAL_SHIELD),
            new TriggerItem(ItemId.CRYSTAL_SHIELD_DEGRADED),
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your crystal shield has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Get hit.
            new OnHitsplatApplied(HitsplatTarget.SELF, HitsplatGroup.SUCCESSFUL).moreThanZeroDamage().isEquipped().decreaseCharges(1)
        };
    }
}
