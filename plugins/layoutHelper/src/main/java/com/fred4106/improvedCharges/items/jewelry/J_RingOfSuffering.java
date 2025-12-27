package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItemWithStatus;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import com.fred4106.improvedCharges.store.Provider;

public class J_RingOfSuffering extends ChargedItemWithStatus {
    public J_RingOfSuffering(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.RING_OF_SUFFERING, ItemId.RING_OF_SUFFERING_UNCHARGED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.RING_OF_SUFFERING_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.RING_OF_SUFFERING_UNCHARGED_IMBUED_NMZ).fixedCharges(0),
            new TriggerItem(ItemId.RING_OF_SUFFERING_UNCHARGED_IMBUED_SW).fixedCharges(0),
            new TriggerItem(ItemId.RING_OF_SUFFERING_UNCHARGED_IMBUED_PVP).fixedCharges(0),
            new TriggerItem(ItemId.RING_OF_SUFFERING).needsToBeEquipped(),
            new TriggerItem(ItemId.RING_OF_SUFFERING_IMBUED_NMZ).needsToBeEquipped(),
            new TriggerItem(ItemId.RING_OF_SUFFERING_IMBUED_SW).needsToBeEquipped(),
            new TriggerItem(ItemId.RING_OF_SUFFERING_IMBUED_PVP).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[]{
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
            new OnChatMessage("The banker charges your Ring of suffering.* using (?<ringofrecoil>.+)x Ring of recoil.").matcherConsumer(m -> {
                final int ringOfRecoils = Integer.parseInt(m.group("ringofrecoil"));
                increaseCharges(ringOfRecoils * 40);
            }),
        };
    }
}
