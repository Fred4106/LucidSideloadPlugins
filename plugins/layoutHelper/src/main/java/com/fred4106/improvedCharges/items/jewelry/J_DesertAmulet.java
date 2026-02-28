package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class J_DesertAmulet extends ChargedItem {
    public J_DesertAmulet(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.DESERT_AMULET, ItemId.DESERT_AMULET_2, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.DESERT_AMULET_2),
            new TriggerItem(ItemId.DESERT_AMULET_3),
            new TriggerItem(ItemId.DESERT_AMULET_4).fixedCharges(ChargeId.UNLIMITED),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("You have already used your available teleports for today.").setFixedCharges(0),
            new OnResetDaily().specificItem(ItemId.DESERT_AMULET_2).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemId.DESERT_AMULET_3).setFixedCharges(1)
        ));
    }
}
