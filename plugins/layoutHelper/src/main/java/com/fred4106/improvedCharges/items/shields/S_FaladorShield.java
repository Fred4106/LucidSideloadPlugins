package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.*;

public class S_FaladorShield extends ChargedItem {
    public S_FaladorShield(Provider provider) {
        super(FredsItemChargesConfig.falador_shield, ItemID.FALADOR_SHIELD_EASY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.FALADOR_SHIELD_EASY),
            new TriggerItem(ItemID.FALADOR_SHIELD_MEDIUM),
            new TriggerItem(ItemID.FALADOR_SHIELD_HARD),
            new TriggerItem(ItemID.FALADOR_SHIELD_ELITE),
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
            new OnResetDaily().specificItem(ItemID.FALADOR_SHIELD_EASY).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemID.FALADOR_SHIELD_MEDIUM).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemID.FALADOR_SHIELD_HARD).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemID.FALADOR_SHIELD_ELITE).setFixedCharges(2)
        ));
    }
}
