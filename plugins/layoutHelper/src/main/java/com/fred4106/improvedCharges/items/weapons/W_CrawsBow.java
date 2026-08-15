package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.ids.*;

import java.util.*;

public class W_CrawsBow extends ChargedItem {
    public W_CrawsBow(Provider provider) {
        this(FredsItemChargesConfig.craws_bow, ItemID.WILD_CAVE_BOW_UNCHARGED, provider);

        this.items = new TriggerItem[]{
                new TriggerItem(ItemID.WILD_CAVE_BOW_UNCHARGED).fixedCharges(0),
                new TriggerItem(ItemID.WILD_CAVE_BOW_CHARGED),
        };
    }

    protected W_CrawsBow(String configKey, int itemId, Provider provider) {
        super(configKey, itemId, provider);

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your bow has (?<charges>.+) charges? left powering it.").onItemClick().setDynamicallyCharges(),

            // Activate.
            new OnChatMessage("You use 1000 ether to activate the weapon.").onItemClick().setFixedCharges(0),

            // Charge.
            new OnChatMessage("You add( a further)? .* revenant ether to your weapon, giving it a total of (?<charges>.+) charges.").onItemClick().setDynamicallyCharges(),

            // Attack.
            new OnAnimationChanged(AnimationID.HUMAN_BOW).isEquipped().decreaseCharges(1)
        ));
    }
}
