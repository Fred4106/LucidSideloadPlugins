package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.FredsItemChargesConfig;
import net.runelite.api.gameval.ItemID;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;

import java.util.List;

public class J_InoculationBracelet extends ChargedItem {
    public J_InoculationBracelet(Provider provider) {
        super(FredsItemChargesConfig.inoculation_bracelet, ItemID.JEWL_BRACELET_OF_INNOCULATION, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.JEWL_BRACELET_OF_INNOCULATION).needsToBeEquipped(),
        };

        this.triggers.addAll(List.of(
            // Check
            new OnChatMessage("Your bracelet will protect you from (?<charges>.+) points? of disease damage.").setDynamicallyCharges(),

            // Break
            new OnChatMessage("Your bracelet of inoculation runs out of charges and crumbles to dust.").setFixedCharges(275),

            // Hitsplat
            new OnHitsplatApplied(HitsplatTarget.SELF, HitsplatGroup.DISEASE_BLOCKED).isEquipped().consumer(() -> {
                decreaseCharges(1);
            })
        ));
    }
}