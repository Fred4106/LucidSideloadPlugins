package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.GraphicId;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.ids.*;

import java.util.*;

public class W_InfernalAxe extends ChargedItem {
    public W_InfernalAxe(Provider provider) {
        super(FredsItemChargesConfig.infernal_axe, ItemID.INFERNAL_AXE_EMPTY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.INFERNAL_AXE_EMPTY).fixedCharges(0),
            new TriggerItem(ItemID.INFERNAL_AXE),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Infernal axe: (?<percentage>.+)% remaining.").matcherConsumer(m -> {
                double percentage = Double.parseDouble(m.group("percentage"));
                setCharges((int) (percentage * 5000 / 100));
            }),

            // Charge used.
            new OnGraphicChanged(GraphicId.INFERNAL_AXE_SMOKE).isEquipped().decreaseCharges(1)
        ));
    }
}
