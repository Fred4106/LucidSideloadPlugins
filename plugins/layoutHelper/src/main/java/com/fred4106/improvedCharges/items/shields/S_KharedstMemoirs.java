package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class S_KharedstMemoirs extends ChargedItem {
    public S_KharedstMemoirs(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.KHAREDSTS_MEMOIRS, ItemId.KHAREDSTS_MEMOIRS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.KHAREDSTS_MEMOIRS),
            new TriggerItem(ItemId.BOOK_OF_THE_DEAD)
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("You add an entry to Kharedst's Memoirs.").increaseCharges(20),

            // Teleport.
            new OnChatMessage("((Kharedst's Memoirs)|(The Book of the Dead)) now has (?<charges>.+) (memories|memory) remaining.").setDynamicallyCharges(),

            // Check empty.
            new OnChatMessage("((Kharedst's Memoirs)|(The Book of the Dead)) holds no charges.").setFixedCharges(0),

            // Check.
            new OnChatMessage("On the inside of the cover a message is displayed in dark ink. It reads: (?<charges>.+)/.+? (memories|memory) remain.").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("((Kharedst's Memoirs)|(The Book of the Dead)) now has (?<charges>.+) charges.").setDynamicallyCharges(),

            // Try to charge book of the dead when already full.
            new OnChatMessage("The Book of the Dead is already fully charged.").setFixedCharges(250),

            // Try to charge kharedst memoirs when already full.
            new OnChatMessage("Kharedst's Memoirs is already fully charged.").setFixedCharges(100),

            // Common menu entries.
            new OnMenuEntryAdded("Reminisce").replaceOption("Teleport"),

            // Hide destroy.
            new OnMenuEntryAdded("Destroy").hide()
        ));
    }
}
