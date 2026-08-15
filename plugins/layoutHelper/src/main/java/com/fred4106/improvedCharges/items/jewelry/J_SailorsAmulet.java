package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuOptionClicked;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_SailorsAmulet extends ChargedItem {
    public J_SailorsAmulet(Provider provider) {
        super(FredsItemChargesConfig.sailors_amulet, ItemID.SAILORS_AMULET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.SAILORS_AMULET_EMPTY).fixedCharges(0),
            new TriggerItem(ItemID.SAILORS_AMULET)
        };

        this.triggers.addAll(List.of(
            // Check
            new OnChatMessage("(The|Your) amulet has (?<charges>.+) charges( left)?.")
                .onItemClick()
                .setDynamicallyCharges(),

            // Charge
            new OnChatMessage("You add .+ charges? to your amulet. It now has (?<charges>.+) charges?.")
                .setDynamicallyCharges(),

            // Teleport
            new OnMenuOptionClicked("The Pandemonium", "Port Roberts", "Deepfin Point")
                .onMenuOptionEventId(65540, 131076, 327684)
                .decreaseCharges(1)
                .multiTrigger(),

            // Teleport while equipped
            new OnMenuOptionClicked("The Pandemonium", "Port Roberts", "Deepfin Point")
                .onItemClick()
                .decreaseCharges(1)
                .multiTrigger(),

            // Teleport location not unlocked
            new OnChatMessage("You must find a sailors' marker at that location before teleporting there.")
                .increaseCharges(1),

            // Auto-charge
            new OnAutoChargeMessage("Sailors' amulet", "Law rune", 10, this)
        ));
    }
}
