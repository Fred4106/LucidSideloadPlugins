package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class W_PharaohsSceptre extends ChargedItem {
    public W_PharaohsSceptre(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.PHARAOHS_SCEPTRE, ItemId.PHARAOHS_SCEPTRE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.PHARAOHS_SCEPTRE_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.PHARAOHS_SCEPTRE_INITIAL),
            new TriggerItem(ItemId.PHARAOHS_SCEPTRE),
        };
        
        this.triggers = new TriggerBase[]{
            // Check and automatic messages.
            new OnChatMessage("Your sceptre has (?<charges>.+) charges? left.").setDynamicallyCharges().onItemClick(),

            // Charge non-empty sceptre.
            new OnChatMessage("Right, you already had .+ charges?, and I don't give discounts. That means .+ artefacts gives you (?<charges>.+) charges?. Now be on your way.").increaseDynamically(),

            // Charge empty sceptre.
            new OnChatMessage("Right, .+ artefacts gives you (?<charges>.+) charges. Now be on your way.").setDynamicallyCharges(),

            // Teleport.
            new OnGraphicChanged(715).decreaseCharges(1),
        };
    }
}
