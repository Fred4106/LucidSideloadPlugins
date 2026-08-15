package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_DesertAmulet extends ChargedItem {
    public J_DesertAmulet(Provider provider) {
        super(FredsItemChargesConfig.desert_amulet, ItemID.DESERT_AMULET_MEDIUM, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.DESERT_AMULET_MEDIUM),
            new TriggerItem(ItemID.DESERT_AMULET_HARD),
            new TriggerItem(ItemID.DESERT_AMULET_ELITE).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("You have already used your available teleports for today.").setFixedCharges(0),
            new OnResetDaily().specificItem(ItemID.DESERT_AMULET_MEDIUM).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemID.DESERT_AMULET_HARD).setFixedCharges(1)
        ));
    }
}
