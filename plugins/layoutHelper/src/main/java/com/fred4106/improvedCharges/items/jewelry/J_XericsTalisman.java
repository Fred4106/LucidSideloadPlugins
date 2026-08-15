package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.OnWidgetLoaded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_XericsTalisman extends ChargedItem {
    public J_XericsTalisman(Provider provider) {
        super(FredsItemChargesConfig.xerics_talisman, ItemID.XERIC_TALISMAN, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.XERIC_TALISMAN_EMPTY).fixedCharges(0),
            new TriggerItem(ItemID.XERIC_TALISMAN),
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
            new OnAutoChargeMessage("Xeric's talisman", "Lizardman fang", 1, this)
        ));
    }
}
