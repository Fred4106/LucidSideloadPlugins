package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.ids.*;

import java.util.*;

public class W_SlayerStaffE extends ChargedItem {
    public W_SlayerStaffE(Provider provider) {
        super(FredsItemChargesConfig.slayer_staff_e, ItemID.SLAYER_STAFF_ENCHANTED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.SLAYER_STAFF_ENCHANTED)
        };

        this.triggers.addAll(List.of(
            // Enchant.
            new OnChatMessage("The spell enchants your staff. The tatty parchment crumbles to dust.").setFixedCharges(2500),

            // Check.
            new OnChatMessage("Your staff has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Attack.
            new OnAnimationChanged(AnimationID.SLAYER_MAGICDART_CAST).isEquipped().decreaseCharges(1)
        ));
    }
}
