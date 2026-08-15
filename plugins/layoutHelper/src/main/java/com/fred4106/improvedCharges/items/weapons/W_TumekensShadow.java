package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
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

public class W_TumekensShadow extends ChargedItem {
    public W_TumekensShadow(Provider provider) {
        super(FredsItemChargesConfig.tumekens_shadow, ItemID.TUMEKENS_SHADOW, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.TUMEKENS_SHADOW_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemID.TUMEKENS_SHADOW),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Tumeken's shadow( only)? has (?<charges>.+) charges? (remaining|left)").setDynamicallyCharges(),

            // Uncharge.
            new OnChatMessage("You uncharge your Tumeken's shadow").setFixedCharges(0),

            // Charge additionally.
            new OnChatMessage("You apply an additional .* charges to your Tumeken's shadow. It now has (?<charges>.+) charges in total.").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You apply (?<charges>.+) charges to your Tumeken's shadow.").setDynamicallyCharges(),

            // Attack.
            new OnGraphicChanged(2125).decreaseCharges(1),

            // Auto-charge.
            new OnAutoChargeMessage("Tumeken's shadow", "Soul rune", 0.5, this)
        ));
    }
}
