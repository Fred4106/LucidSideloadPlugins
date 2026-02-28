package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class J_XericsTalisman extends ChargedItem {
    public J_XericsTalisman(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.XERICS_TALISMAN, ItemId.XERICS_TALISMAN, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.XERICS_TALISMAN_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.XERICS_TALISMAN),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("(The|Your) talisman( now)? has one charge.").onItemClick().setFixedCharges(1),
            new OnChatMessage("(The|Your) talisman( now)? has (?<charges>.+) charges.").setDynamicallyCharges().onItemClick(),

            // Teleport.
            new OnGraphicChanged(1612).decreaseCharges(1),

            // Teleport widget.
            new OnWidgetLoaded(187, 0, 1).text("The talisman has (?<charges>.+) charges.").setDynamically(),

            // Unified menu entry.
            new OnMenuEntryAdded("Rub").replaceOption("Teleport"),

            // Auto-charge.
            new OnChatMessage("The banker charges your Xeric's talisman using (?<lizardmanfang>.+)x Lizardman fang.").matcherConsumer(m -> {
                final int lizardmanFangs = Integer.parseInt(m.group("lizardmanfang"));
                increaseCharges(lizardmanFangs);
            })
        ));
    }
}
