package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_RingOfShadows extends ChargedItem {
    public J_RingOfShadows(Provider provider) {
        super(FredsItemChargesConfig.ring_of_shadows, ItemID.RING_OF_SHADOWS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.RING_OF_SHADOWS_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemID.RING_OF_SHADOWS)
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your ring of shadows has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You add (?<charges>.+) charges? to the ring of shadows.$").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You add .+ charges? to the ring of shadows. It now has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Teleport.
            new OnAnimationChanged(AnimationID.MAHJARRAT_HUMAN_DISAPPEAR_01_NOHELD_QUICK).decreaseCharges(1),

            // Auto-charge.
            new OnAutoChargeMessage("Ring of shadows", "Blood rune", 1, this)
        ));
    }
}
