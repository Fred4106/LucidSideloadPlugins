package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class J_GiantsoulAmulet extends ChargedItem {
    public J_GiantsoulAmulet(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.GIANTSOUL_AMULET, ItemId.GIANTSOUL_AMULET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.GIANTSOUL_AMULET_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.GIANTSOUL_AMULET),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your Giantsoul amulet has (?<charges>.+) charges? left powering it.").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You add .+ charges? to your Giantsoul amulet, giving it a total of (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Teleport.
            new OnGraphicChanged(3226).decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Giantsoul amulet using (?<bigbones>.+)x Big bones.*").matcherConsumer(m -> {
                final int bigBones = Integer.parseInt(m.group("bigbones"));
                increaseCharges(bigBones);
            }),

            // Unified menu entry.
            new OnMenuEntryAdded("Rub").replaceOption("Teleport")
        ));
    }
}
