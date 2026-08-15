package com.fred4106.improvedCharges.items.helms;

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

import java.util.*;

public class H_KandarinHeadgear extends ChargedItem {
    public H_KandarinHeadgear(Provider provider) {
        super(FredsItemChargesConfig.kandarin_headgear, ItemID.SEERS_HEADBAND_HARD, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.SEERS_HEADBAND_HARD),
            new TriggerItem(ItemID.SEERS_HEADBAND_ELITE).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            // Try to teleport while empty.
            new OnChatMessage("You have already used your available teleports for today. Your headgear will recharge tomorrow.").onItemClick().setFixedCharges(0),

            // Teleport.
            new OnGraphicChanged(111).onItemClick().decreaseCharges(1),

            // Daily reset.
            new OnResetDaily().specificItem(ItemID.SEERS_HEADBAND_HARD).setFixedCharges(1)
        ));
    }
}
