package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
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

public class J_AlchemistsAmulet extends ChargedItem {
    public J_AlchemistsAmulet(Provider provider) {
        super(FredsItemChargesConfig.alchemists_amulet, ItemID.AMULET_OF_CHEMISTRY_IMBUED_CHARGED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.AMULET_OF_CHEMISTRY_IMBUED_CHARGED).needsToBeEquipped(),
            new TriggerItem(ItemID.AMULET_OF_CHEMISTRY_IMBUED_UNCHARGED).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            // Check
            new OnChatMessage("Your Alchemist's amulet has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Charge
            new OnChatMessage("You apply an additional .+ charges to your Alchemist's amulet. It now has (?<charges>.+) charges in total.").setDynamicallyCharges(),
            new OnChatMessage("You apply (?<charges>.+) charges to your Alchemist's amulet.").setDynamicallyCharges(),

            // Uncharge
            new OnChatMessage("You uncharge your Alchemist's amulet, regaining .+ amulets of chemistry in the process.").setFixedCharges(0),

            // Use charge
            new OnChatMessage("Your Alchemist's amulet helps you create a .-dose potion. It no longer has any charges.").setFixedCharges(0),
            new OnChatMessage("Your Alchemist's amulet helps you create a .-dose potion. It has one charge left.").setFixedCharges(1),
            new OnChatMessage("Your Alchemist's amulet helps you create a .-dose potion. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Auto-charge
            new OnAutoChargeMessage("Alchemist's amulet", "Amulet of chemistry", 10, this)
        ));
    }
}
