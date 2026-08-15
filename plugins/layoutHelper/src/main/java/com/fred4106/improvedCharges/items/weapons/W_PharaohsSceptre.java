package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_PharaohsSceptre extends ChargedItem {
    public W_PharaohsSceptre(Provider provider) {
        super(FredsItemChargesConfig.pharaohs_sceptre, ItemID.PHARAOHS_SCEPTRE_CHARGED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.PHARAOHS_SCEPTRE).fixedCharges(0),
            new TriggerItem(ItemID.PHARAOHS_SCEPTRE_CHARGED_INITIAL),
            new TriggerItem(ItemID.PHARAOHS_SCEPTRE_CHARGED),
        };

        this.triggers.addAll(List.of(
            // Check and automatic messages.
            new OnChatMessage("Your sceptre has (?<charges>.+) charges? left.").setDynamicallyCharges().onItemClick(),

            // Charge non-empty sceptre.
            new OnChatMessage("Right, you already had .+ charges?, and I don't give discounts. That means .+ artefacts gives you (?<charges>.+) charges?. Now be on your way.").increaseDynamically(),

            // Charge empty sceptre.
            new OnChatMessage("Right, .+ artefacts gives you (?<charges>.+) charges. Now be on your way.").setDynamicallyCharges(),

            // Teleport.
            new OnGraphicChanged(715).decreaseCharges(1)
        ));
    }
}
