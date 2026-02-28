package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.store.ids.AnimationId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class J_RingOfShadows extends ChargedItem {
    public J_RingOfShadows(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.RING_OF_SHADOWS, ItemId.RING_OF_SHADOWS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.RING_OF_SHADOWS_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.RING_OF_SHADOWS)
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your ring of shadows has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You add (?<charges>.+) charges? to the ring of shadows.$").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You add .+ charges? to the ring of shadows. It now has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Teleport.
            new OnAnimationChanged(AnimationId.RING_OF_SHADOWS_TELEPORT).decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Ring of shadows using (?<bloodrune>.+)x Blood rune.*").matcherConsumer(m -> {
                final int bloodRunes = Integer.parseInt(m.group("ringofrecoil"));
                increaseCharges(bloodRunes);
            })
        ));
    }
}
