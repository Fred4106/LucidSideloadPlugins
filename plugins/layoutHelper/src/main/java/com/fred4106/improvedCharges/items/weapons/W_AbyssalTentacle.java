package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;

import java.util.List;

public class W_AbyssalTentacle extends ChargedItem {
    public W_AbyssalTentacle(final Provider provider) {
        super(Constants.ABYSSAL_TENTACLE, ItemId.ABYSSAL_TENTACLE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ABYSSAL_TENTACLE),
            new TriggerItem(ItemId.LEAGUE_3_WHIP_TENTACLE)
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your abyssal tentacle can perform (?<charges>.+) more attacks?").setDynamicallyCharges(),

            // Attack.
            new OnAnimationChanged(1658).itemEquipped().decreaseCharges(1),

            // Degrade
            new OnChatMessage("Your abyssal tentacle has degraded.").setFixedCharges(0)
        ));
    }
}
