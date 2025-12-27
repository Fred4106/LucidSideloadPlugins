package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.Provider;

public class W_WesternBanner extends ChargedItem {
    public W_WesternBanner(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.WESTERN_BANNER, ItemId.WESTERN_BANNER_3, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.WESTERN_BANNER_3),
            new TriggerItem(ItemId.WESTERN_BANNER_4).fixedCharges(ChargeId.UNLIMITED),
        };

        this.triggers = new TriggerBase[]{
            // Teleport.
            new OnMenuOptionClicked("Teleport").hasItemId(ItemId.WESTERN_BANNER_3).setFixedCharges(0),

            // Teleport already used.
            new OnChatMessage("You have already used your available teleports for today. Try again tomorrow after the standard has recharged.").onItemClick().setFixedCharges(0),

            // Daily reset.
            new OnResetDaily().requiredItem(ItemId.WESTERN_BANNER_3).setFixedCharges(1),
        };
    }
}
