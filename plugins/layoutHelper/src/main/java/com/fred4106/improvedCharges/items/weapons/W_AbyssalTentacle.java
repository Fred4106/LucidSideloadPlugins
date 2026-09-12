package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_AbyssalTentacle extends ChargedItem {
    public W_AbyssalTentacle(Provider provider) {
        super(FredsItemChargesConfig.abyssal_tentacle, ItemID.ABYSSAL_TENTACLE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.ABYSSAL_TENTACLE),
            new TriggerItem(ItemID.LEAGUE_3_WHIP_TENTACLE),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your abyssal tentacle can perform (?<charges>.+) more attacks?.").setDynamicallyCharges(),

            // Attack.
            new OnAnimationChanged(1658).isEquipped().decreaseCharges(1),

            // Degrade
            new OnChatMessage("Your abyssal tentacle has degraded.").setFixedCharges(0)
        ));
    }
}
