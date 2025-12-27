package com.fred4106.improvedCharges.items.helms;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.Provider;

public class H_KandarinHeadgear extends ChargedItem {
    public H_KandarinHeadgear(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.KANDARIN_HEADGEAR, ItemId.KANDARIN_HEADGEAR_3, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.KANDARIN_HEADGEAR_3),
            new TriggerItem(ItemId.KANDARIN_HEADGEAR_4).fixedCharges(ChargeId.UNLIMITED),
        };

        this.triggers = new TriggerBase[] {
            // Try to teleport while empty.
            new OnChatMessage("You have already used your available teleports for today. Your headgear will recharge tomorrow.").onItemClick().setFixedCharges(0),

            // Teleport.
            new OnGraphicChanged(111).onItemClick().decreaseCharges(1),

            // Daily reset.
            new OnResetDaily().specificItem(ItemId.KANDARIN_HEADGEAR_3).setFixedCharges(1),
        };
    }
}
