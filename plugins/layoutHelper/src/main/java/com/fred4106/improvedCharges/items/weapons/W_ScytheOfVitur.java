package com.fred4106.improvedCharges.items.weapons;

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

public class W_ScytheOfVitur extends ChargedItem {
    public W_ScytheOfVitur(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.SCYTHE_OF_VITUR, ItemId.SCYTHE_OF_VITUR, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.SCYTHE_OF_VITUR),
            new TriggerItem(ItemId.SCYTHE_OF_VITUR_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.HOLY_SCYTHE_OF_VITUR),
            new TriggerItem(ItemId.HOLY_SCYTHE_OF_VITUR_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.SANGUINE_SCYTHE_OF_VITUR),
            new TriggerItem(ItemId.SANGUINE_SCYTHE_OF_VITUR_UNCHARGED).fixedCharges(0),
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("Your (Holy s|Sanguine s|[Ss])cythe (of vitur )?has (?<charges>.+) charges (remaining|left).").setDynamicallyCharges(),

            // Charge partially full.
            new OnChatMessage("You apply an additional .+ charges to your (Holy s|Sanguine s|S)cythe of vitur. It now has (?<charges>.+) charges in total.").setDynamicallyCharges(),

            // Charge empty.
            new OnChatMessage("You apply (?<charges>.+) charges to your (Holy s|Sanguine s|S)cythe of vitur.").setDynamicallyCharges(),

            // Attack.
            new OnHitsplatApplied(HitsplatTarget.ENEMY, HitsplatGroup.SUCCESSFUL).moreThanZeroDamage().oncePerGameTick().isEquipped().decreaseCharges(1),
        };
    }
}
