package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItemWithStatus;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.enums.*;

import java.util.*;

public class J_RingOfSuffering extends ChargedItemWithStatus {
    public J_RingOfSuffering(Provider provider) {
        super(FredsItemChargesConfig.ring_of_suffering, ItemID.ZENYTE_RING_ENCHANTED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.ZENYTE_RING_ENCHANTED).fixedCharges(0),
            new TriggerItem(ItemID.NZONE_ZENYTE_RING_ENCHANTED).fixedCharges(0),
            new TriggerItem(ItemID.SW_ZENYTE_RING_ENCHANTED).fixedCharges(0),
            new TriggerItem(ItemID.PVPA_ZENYTE_RING_ENCHANTED).fixedCharges(0),
            new TriggerItem(ItemID.ZENYTE_RING_ENCHANTED_RECOIL).needsToBeEquipped(),
            new TriggerItem(ItemID.NZONE_ZENYTE_RING_ENCHANTED_RECOIL).needsToBeEquipped(),
            new TriggerItem(ItemID.SW_ZENYTE_RING_ENCHANTED_RECOIL).needsToBeEquipped(),
            new TriggerItem(ItemID.PVPA_ZENYTE_RING_ENCHANTED_RECOIL).needsToBeEquipped(),
        };

        this.triggers.addAll(List.of(
            // Check
            new OnChatMessage("Your ring currently has (?<charges>.+) recoil charges? remaining. The recoil effect is currently enabled.").setDynamicallyCharges().onItemClick().activate(),
            new OnChatMessage("Your ring currently has (?<charges>.+) recoil charges? remaining. The recoil effect is currently disabled.").setDynamicallyCharges().onItemClick().deactivate(),

            // Charge
            new OnChatMessage("You load your ring with .+ rings? of recoil. It now has (?<charges>.+) recoil charges.").setDynamicallyCharges(),

            // Get hit.
            new OnHitsplatApplied(HitsplatTarget.SELF, HitsplatGroup.SUCCESSFUL).moreThanZeroDamage().isEquipped().isActivated().decreaseCharges(1),

            // Disable.
            new OnChatMessage("You disable the recoil effect of your ring.").deactivate(),

            // Enable.
            new OnChatMessage("You enable the recoil effect of your ring.").activate(),

            // Auto-charge.
            new OnAutoChargeMessage("Ring of suffering.*", "Ring of recoil", 40, this)
        ));
    }
}
