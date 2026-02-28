package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class S_FaladorShield extends ChargedItem {
    public S_FaladorShield(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.FALADOR_SHIELD, ItemId.FALADOR_SHIELD_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.FALADOR_SHIELD_1),
            new TriggerItem(ItemId.FALADOR_SHIELD_2),
            new TriggerItem(ItemId.FALADOR_SHIELD_3),
            new TriggerItem(ItemId.FALADOR_SHIELD_4),
        };
        
        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("You have one remaining charge for today.").onItemClick().setFixedCharges(1),
            new OnChatMessage("You have two remaining charges for today.").onItemClick().setFixedCharges(2),

            // Teleport when empty.
            new OnChatMessage("You have already used (both )?your charge(s)? for today.").onItemClick().setFixedCharges(0),
            new OnChatMessage("You have already used all available recharges today. Try again tomorrow when the shield has recharged.").onItemClick().setFixedCharges(0),

            // Recharge prayer.
            new OnGraphicChanged(321).onItemClick().decreaseCharges(1),

            // Daily resets.
            new OnResetDaily().specificItem(ItemId.FALADOR_SHIELD_1).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemId.FALADOR_SHIELD_2).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemId.FALADOR_SHIELD_3).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemId.FALADOR_SHIELD_4).setFixedCharges(2)
        ));
    }
}
