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

public class W_BowOfFaerdhinen extends ChargedItem {
    public W_BowOfFaerdhinen(Provider provider) {
        super(FredsItemChargesConfig.bow_of_faerdhinen, ItemID.BOW_OF_FAERDHINEN, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE).unlimitedCharges(),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE_ITHELL).unlimitedCharges(),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE_IORWERTH).unlimitedCharges(),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE_TRAHAEARN).unlimitedCharges(),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE_CADARN).unlimitedCharges(),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE_CRWYS).unlimitedCharges(),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE_MEILYR).unlimitedCharges(),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE_AMLODD).unlimitedCharges(),
            new TriggerItem(ItemID.BOW_OF_FAERDHINEN_INFINITE_DEADMAN).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your bow of Faerdhinen has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Attack.
            new OnGraphicChanged(1888).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnAutoChargeMessage("Bow of faerdhinen", "Crystal shard", 100, this)
        ));
    }
}
