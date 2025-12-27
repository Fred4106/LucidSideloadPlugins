package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.Provider;

public class J_AmuletOfGlory extends ChargedItem {
    public J_AmuletOfGlory(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.AMULET_OF_GLORY, ItemId.AMULET_OF_GLORY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.AMULET_OF_GLORY).fixedCharges(0),
            new TriggerItem(ItemId.AMULET_OF_GLORY_1).fixedCharges(1),
            new TriggerItem(ItemId.AMULET_OF_GLORY_2).fixedCharges(2),
            new TriggerItem(ItemId.AMULET_OF_GLORY_3).fixedCharges(3),
            new TriggerItem(ItemId.AMULET_OF_GLORY_4).fixedCharges(4),
            new TriggerItem(ItemId.AMULET_OF_GLORY_5).fixedCharges(5),
            new TriggerItem(ItemId.AMULET_OF_GLORY_6).fixedCharges(6),
            new TriggerItem(ItemId.AMULET_OF_GLORY_ETERNAL).fixedCharges(ChargeId.UNLIMITED),
            new TriggerItem(ItemId.AMULET_OF_GLORY_TRIMMED).fixedCharges(0),
            new TriggerItem(ItemId.AMULET_OF_GLORY_TRIMMED_1).fixedCharges(1),
            new TriggerItem(ItemId.AMULET_OF_GLORY_TRIMMED_2).fixedCharges(2),
            new TriggerItem(ItemId.AMULET_OF_GLORY_TRIMMED_3).fixedCharges(3),
            new TriggerItem(ItemId.AMULET_OF_GLORY_TRIMMED_4).fixedCharges(4),
            new TriggerItem(ItemId.AMULET_OF_GLORY_TRIMMED_5).fixedCharges(5),
            new TriggerItem(ItemId.AMULET_OF_GLORY_TRIMMED_6).fixedCharges(6),
        };

        this.triggers = new TriggerBase[] {
            // Check
            new OnChatMessage("Your Alchemist's amulet has (?<charges>.+) charges left.").setDynamicallyCharges(),

            // Charge
            new OnChatMessage("You apply an additional .+ charges to your Alchemist's amulet. It now has (?<charges>.+) charges in total.").setDynamicallyCharges(),
            new OnChatMessage("You apply (?<charges>.+) charges to your Alchemist's amulet.").setDynamicallyCharges(),

            // Uncharge
            new OnChatMessage("You uncharge your Alchemist's amulet, regaining .+ amulets of chemistry in the process.").setFixedCharges(0),

            // Use charge
            new OnChatMessage("Your Alchemist's amulet helps you create a 4-dose potion. It no longer has any charges.").setFixedCharges(0),
            new OnChatMessage("Your Alchemist's amulet helps you create a 4-dose potion. It has one charge left.").setFixedCharges(1),
            new OnChatMessage("Your Alchemist's amulet helps you create a 4-dose potion. It has (?<charges>.+) charges left.").setDynamicallyCharges(),

            // Auto-charge
            new OnChatMessage("The banker charges your Alchemist's amulet using (?<amulets>.+)x Amulet of chemistry.*").matcherConsumer(m -> {
                final int amuletsOfChemistry = Integer.parseInt(m.group("amulets"));
                increaseCharges(amuletsOfChemistry * 10);
            }),
        };
    }
}
