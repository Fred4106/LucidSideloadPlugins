package com.fred4106.improvedCharges.items.capes;

import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.store.Provider;

public class C_MagicCape extends ChargedItem {
    public C_MagicCape(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.MAGIC_CAPE, ItemId.MAGIC_CAPE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.MAGIC_CAPE),
            new TriggerItem(ItemId.MAGIC_CAPE_TRIMMED)
        };

        this.triggers = new TriggerBase[] {
            // After spellbook swap.
            new OnChatMessage("You have changed your spellbook (?<used>.+)/(?<total>.+) times today.").setDifferenceCharges(),

            // Spellbook swap widget.
            new OnWidgetLoaded(219, 1, 0).text("Choose spellbook: \\((?<charges>.+)/5 left\\)").setDynamically(),

            // Daily reset.
            new OnResetDaily().setFixedCharges(5),
        };
    }
}
