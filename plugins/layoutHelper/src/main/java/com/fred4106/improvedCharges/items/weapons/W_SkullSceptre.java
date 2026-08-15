package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_SkullSceptre extends ChargedItem {
    public W_SkullSceptre(Provider provider) {
        super(FredsItemChargesConfig.skull_sceptre, ItemID.SOS_SKULL_SCEPTRE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.SOS_SKULL_SCEPTRE),
            new TriggerItem(ItemID.SOS_SKULL_SCEPTRE_IMBUED)
        };

        this.triggers.addAll(List.of(
            // Teleport.
            new OnChatMessage("Your Skull Sceptre has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Check.
            new OnChatMessage("Concentrating deeply, you divine that the sceptre has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Charge to maximum without all Varrock diaries completed.
            new OnChatMessage("You charge the Skull Sceptre with .*. It now contains the maximum number of charges, (?<charges>.+). Completing tiers of the Varrock achievement diary will allow the imbued Skull Sceptre to hold more charges.").setDynamicallyCharges(),

            // Charge to maximum.
            new OnChatMessage("You charge the Skull Sceptre with .*. It now contains the maximum number of charges, (?<charges>.+).").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You charge the Skull Sceptre with .*. It now contains (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Unified menu entry.
            new OnMenuEntryAdded("Divine").replaceOption("Check"),
            new OnMenuEntryAdded("Invoke").replaceOption("Teleport")
        ));
    }
}
