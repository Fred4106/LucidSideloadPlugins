package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuOptionClicked;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_WesternBanner extends ChargedItem {
    public W_WesternBanner(Provider provider) {
        super(FredsItemChargesConfig.western_banner, ItemID.WESTERN_BANNER_HARD, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.WESTERN_BANNER_HARD),
            new TriggerItem(ItemID.WESTERN_BANNER_ELITE).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            // Teleport.
            new OnMenuOptionClicked("Teleport").hasItemId(ItemID.WESTERN_BANNER_HARD).setFixedCharges(0),

            // Teleport already used.
            new OnChatMessage("You have already used your available teleports for today. Try again tomorrow after the standard has recharged.").onItemClick().setFixedCharges(0),

            // Daily reset.
            new OnResetDaily().requiredItem(ItemID.WESTERN_BANNER_HARD).setFixedCharges(1)
        ));
    }
}
