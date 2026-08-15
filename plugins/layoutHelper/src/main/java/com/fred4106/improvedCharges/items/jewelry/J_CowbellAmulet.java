package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_CowbellAmulet extends ChargedItem {
    public J_CowbellAmulet(Provider provider) {
        super(FredsItemChargesConfig.cowbell_amulet, ItemID.COWBELL_AMULET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.COWBELL_AMULET_EMPTY).fixedCharges(0),
            new TriggerItem(ItemID.COWBELL_AMULET)
        };

        this.triggers.addAll(List.of(
            // Check
            new OnChatMessage("The amulet has (?<charges>.+) charges?.").onItemClick().setDynamicallyCharges(),

            // Auto message
            new OnChatMessage("Your amulet has (?<charges>.+) charges? left.").onItemClick().setDynamicallyCharges(),

            // Charge
            new OnChatMessage("You add .* air runes? to your amulet. It now has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Teleport
            new OnAnimationChanged(13811).onMenuOption("Teleport").decreaseCharges(1),

            // Auto-charge
            new OnAutoChargeMessage("Cowbell amulet", "Air rune", 1, this)
        ));
    }
}
