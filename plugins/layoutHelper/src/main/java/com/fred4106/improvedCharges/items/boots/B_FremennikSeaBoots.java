package com.fred4106.improvedCharges.items.boots;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.List;

public class B_FremennikSeaBoots extends ChargedItem {
    public B_FremennikSeaBoots(Provider provider) {
        super(FredsItemChargesConfig.fremennik_sea_boots, ItemID.FREMENNIK_BOOTS_EASY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.FREMENNIK_BOOTS_EASY),
            new TriggerItem(ItemID.FREMENNIK_BOOTS_MEDIUM),
            new TriggerItem(ItemID.FREMENNIK_BOOTS_HARD),
            new TriggerItem(ItemID.FREMENNIK_BOOTS_ELITE).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            // Try to teleport while empty.
            new OnChatMessage("You have already used your available teleport for today. Try again tomorrow when the boots have recharged.").setFixedCharges(0),

            // Teleport.
            new OnGraphicChanged(111).onItemClick().decreaseCharges(1),

            // Daily reset.
            new OnResetDaily().specificItem(ItemID.FREMENNIK_BOOTS_EASY).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemID.FREMENNIK_BOOTS_MEDIUM).setFixedCharges(3),
            new OnResetDaily().specificItem(ItemID.FREMENNIK_BOOTS_HARD).setFixedCharges(5)
        ));
    }
}