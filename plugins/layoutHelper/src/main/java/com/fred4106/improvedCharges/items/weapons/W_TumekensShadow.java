package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class W_TumekensShadow extends ChargedItem {
    public W_TumekensShadow(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.TUMEKENS_SHADOW, ItemId.TUMEKENS_SHADOW, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TUMEKENS_SHADOW_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.TUMEKENS_SHADOW),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Tumeken's shadow( only)? has (?<charges>.+) charges? (remaining|left)").setDynamicallyCharges(),

            // Uncharge.
            new OnChatMessage("You uncharge your Tumeken's shadow").setFixedCharges(0),

            // Charge.
            new OnChatMessage("You apply (?<charges>.+) to your Tumeken's shadow.").setDynamicallyCharges(),

            // Charge additionally.
            new OnChatMessage("You apply an additional .* charges to your Tumeken's shadow. It now has (?<charges>.+) charges in total.").setDynamicallyCharges(),

            // Attack.
            new OnGraphicChanged(2125).decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Tumeken's shadow using (?<soulrune>.+)x Soul rune").matcherConsumer(m -> {
                final int soulRunes = Integer.parseInt(m.group("soulrune"));
                increaseCharges(soulRunes / 2);
            })
        ));
    }
}
