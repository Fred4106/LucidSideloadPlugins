package com.fred4106.improvedCharges.items.capes;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.OnWidgetLoaded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class C_MagicCape extends ChargedItem {
    public C_MagicCape(Provider provider) {
        super(FredsItemChargesConfig.magic_cape, ItemID.SKILLCAPE_MAGIC, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.SKILLCAPE_MAGIC),
            new TriggerItem(ItemID.SKILLCAPE_MAGIC_TRIMMED)
        };

        this.triggers.addAll(List.of(
            // After spellbook swap.
            new OnChatMessage("You have changed your spellbook (?<used>.+)/(?<total>.+) times today.").setDifferenceCharges(),

            // Spellbook swap widget.
            new OnWidgetLoaded(219, 1, 0).text("Choose spellbook: \\((?<charges>.+)/5 left\\)").setDynamically(),

            // Daily reset.
            new OnResetDaily().setFixedCharges(5)
        ));
    }
}
