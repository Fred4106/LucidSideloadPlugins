package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import net.runelite.api.Skill;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItemWithStatus;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.OnXpDrop;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class U_BoneCrusher extends ChargedItemWithStatus {
    public U_BoneCrusher(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.BONECRUSHER, ItemId.BONECRUSHER, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.BONECRUSHER),
            new TriggerItem(ItemId.BONECRUSHER_NECKLACE)
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("The bonecrusher( necklace)? has no charges.").setFixedCharges(0),
            new OnChatMessage("The bonecrusher( necklace)? has one charge.").setFixedCharges(1),
            new OnChatMessage("(The|Your) bonecrusher( necklace)? has (?<charges>.+) charges?( left)?. It is active").setDynamicallyCharges().activate(),
            new OnChatMessage("(The|Your) bonecrusher( necklace)? has (?<charges>.+) charges?( left)?. It has been deactivated").setDynamicallyCharges().deactivate(),
            new OnChatMessage("(The|Your) bonecrusher( necklace)? has (?<charges>.+) charges?( left)?.").setDynamicallyCharges(),

            // Uncharge.
            new OnChatMessage("You remove all the charges from the bonecrusher( necklace)?.").setFixedCharges(0),

            // Ran out.
            new OnChatMessage("Your bonecrusher( necklace)? has run out of charges.").setFixedCharges(0),

            // Activate.
            new OnChatMessage("The bonecrusher( necklace)? has been deactivated").deactivate(),

            // Deactivate.
            new OnChatMessage("The bonecrusher( necklace)? is active").activate(),

            // Automatic bury.
            new OnXpDrop(Skill.PRAYER).isActivated().decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Bonecrusher( necklace)? using (?<ectotoken>.+)x Ecto-token.").matcherConsumer(m -> {
                final int ectoTokens = Integer.parseInt(m.group("ectotoken"));
                increaseCharges(ectoTokens * 25);
            }),

            // Hide destroy.
            new OnMenuEntryAdded("Destroy").hide(),
        };
    }
}
