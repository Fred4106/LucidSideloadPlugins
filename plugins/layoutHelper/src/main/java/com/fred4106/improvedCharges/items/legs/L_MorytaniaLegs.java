package com.fred4106.improvedCharges.items.legs;

import com.fred4106.improvedCharges.FredsItemChargesConfig;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.ItemID;

import java.util.List;

public class L_MorytaniaLegs extends ChargedItem {
    public L_MorytaniaLegs(Provider provider) {
        super(FredsItemChargesConfig.morytania_legs, ItemID.MORYTANIA_LEGS_HARD, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.MORYTANIA_LEGS_HARD),
            new TriggerItem(ItemID.MORYTANIA_LEGS_ELITE).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            // Teleport.
            new OnChatMessage("You have (?<charges>.+) teleports? remaining for today.").onItemClick().setDynamicallyCharges(),

            // Daily resets.
            new OnResetDaily().specificItem(ItemID.MORYTANIA_LEGS_HARD).setFixedCharges(5)
        ));
    }
}