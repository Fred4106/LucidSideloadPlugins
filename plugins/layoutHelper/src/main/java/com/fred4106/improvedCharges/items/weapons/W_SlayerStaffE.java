package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.AnimationId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class W_SlayerStaffE extends ChargedItem {
    public W_SlayerStaffE(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.SLAYER_STAFF_E, ItemId.SLAYER_STAFF_ENCHANTED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.SLAYER_STAFF_ENCHANTED)
        };

        this.triggers.addAll(List.of(
            // Enchant.
            new OnChatMessage("The spell enchants your staff. The tatty parchment crumbles to dust.").setFixedCharges(2500),

            // Check.
            new OnChatMessage("Your staff has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Attack.
            new OnAnimationChanged(AnimationId.SLAYER_STAFF_CAST).isEquipped().decreaseCharges(1)
        ));
    }
}
