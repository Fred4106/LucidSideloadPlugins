package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_WarpedSceptre extends ChargedItem {
    public W_WarpedSceptre(Provider provider) {
        super(FredsItemChargesConfig.warped_sceptre, ItemID.WARPED_SCEPTRE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.WARPED_SCEPTRE_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemID.WARPED_SCEPTRE)
        };

        this.triggers.addAll(List.of(
            // Charge additional.
            new OnChatMessage("You add an additional .+ charges? to your warped sceptre. It now has (?<charges>.+) charges in total.").setDynamicallyCharges(),

            // Charge empty.
            new OnChatMessage("You add (?<charges>.+) charges? to your warped sceptre.").setDynamicallyCharges(),

            // Check.
            new OnChatMessage("Your warped sceptre( only)? has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Attack.
            new OnGraphicChanged(2567).decreaseCharges(1),

            // Uncharge.
            new OnChatMessage("You uncharge your warped sceptre").setFixedCharges(0),

            // Ran out of charges.
            new OnChatMessage("Your warped sceptre has run out of charges!").setFixedCharges(0),

            // Auto-charge.
            new OnAutoChargeMessage("Warped sceptre", "Chaos rune", 0.5, this)
        ));
    }
}
