package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStatus;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.OnXpDrop;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import net.runelite.api.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.*;

public class U_AshSanctifier extends ChargedItemWithStatus {
    public U_AshSanctifier(Provider provider) {
        super(FredsItemChargesConfig.ash_sanctifier, ItemID.ASH_SANCTIFIER, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.ASH_SANCTIFIER),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("(The|Your) ash sanctifier has (?<charges>.+) charges?( left)?. It has been deactivated").setDynamicallyCharges().deactivate(),
            new OnChatMessage("(The|Your) ash sanctifier has (?<charges>.+) charges?( left)?. It is active").setDynamicallyCharges().activate(),
            new OnChatMessage("(The|Your) ash sanctifier has (?<charges>.+) charges?( left)?.").setDynamicallyCharges(),

            // Activate.
            new OnChatMessage("The ash sanctifier is active and ready to scatter ashes.").activate(),

            // Deactivate.
            new OnChatMessage("The ash sanctifier has been deactivated, and will not scatter ashes now.").deactivate(),

            // Automatic scatter.
            new OnXpDrop(Skill.PRAYER).isActivated().decreaseCharges(1),

            // Hide destroy.
            new OnMenuEntryAdded("Destroy").hide(),

            // Auto-charge.
            new OnAutoChargeMessage("Ash sanctifier", "Death rune", 10, this)
        ));
    }
}
