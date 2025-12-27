package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.GraphicId;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class W_InfernalAxe extends ChargedItem {
    public W_InfernalAxe(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.INFERNAL_AXE, ItemId.INFERNAL_AXE_UNCHARGED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.INFERNAL_AXE_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.INFERNAL_AXE),
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Infernal axe: (?<percentage>.+)% remaining.").matcherConsumer(m -> {
                final double percentage = Double.parseDouble(m.group("percentage"));
                setCharges((int) (percentage * 5000 / 100));
            }),

            // Charge used.
            new OnGraphicChanged(GraphicId.INFERNAL_AXE_SMOKE).isEquipped().decreaseCharges(1),
        };
    }
}
