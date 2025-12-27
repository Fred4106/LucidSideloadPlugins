package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.AnimationId;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class W_CrawsBow extends ChargedItem {
    public W_CrawsBow(final Provider provider) {
        this(com.fred4106.improvedCharges.Constants.CRAWS_BOW, ItemId.CRAWS_BOW_UNCHARGED, provider);

        this.items = new TriggerItem[]{
                new TriggerItem(ItemId.CRAWS_BOW_UNCHARGED).fixedCharges(0),
                new TriggerItem(ItemId.CRAWS_BOW),
        };
    }

    protected W_CrawsBow(final String configKey, final int itemId, final Provider provider) {
        super(configKey, itemId, provider);

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your bow has (?<charges>.+) charges? left powering it.").onItemClick().setDynamicallyCharges(),

            // Activate.
            new OnChatMessage("You use 1000 ether to activate the weapon.").onItemClick().setFixedCharges(0),

            // Charge.
            new OnChatMessage("You add( a further)? .* revenant ether to your weapon, giving it a total of (?<charges>.+) charges.").onItemClick().setDynamicallyCharges(),

            // Attack.
            new OnAnimationChanged(AnimationId.HUMAN_BOW).isEquipped().decreaseCharges(1),
        };
    }
}
