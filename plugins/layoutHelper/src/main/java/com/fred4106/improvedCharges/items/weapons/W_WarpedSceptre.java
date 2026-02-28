package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class W_WarpedSceptre extends ChargedItem {
    public W_WarpedSceptre(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.WARPED_SCEPTRE, ItemId.WARPED_SCEPTRE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.WARPED_SCEPTRE_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.WARPED_SCEPTRE)
        };

        this.triggers.addAll(List.of(
            // Charge additional.
            new OnChatMessage("You add an additional .+ charges? to your warped sceptre. It now has (?<charges>.+) charges in total.").setDynamicallyCharges(),

            // Charge empty.
            new OnChatMessage("You add (?<charges>.+) charges? to your warped sceptre.").setDynamicallyCharges(),

            // Check.
            new OnChatMessage("Your warped sceptre( only)? has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Attack.
            new OnGraphicChanged(2567).decreaseCharges(1),

            // Uncharge.
            new OnChatMessage("You uncharge your warped sceptre").setFixedCharges(0),

            // Ran out of charges.
            new OnChatMessage("Your warped sceptre has run out of charges!").setFixedCharges(0),

            // Auto-charge.
            new OnChatMessage("The banker charges your Warped sceptre using (?<chaosrune>.+)x Chaos rune, and .+x Earth rune.*").matcherConsumer(m -> {
                final int chaosRunes = Integer.parseInt(m.group("chaosrune"));
                increaseCharges(chaosRunes / 2);
            })
        ));
    }
}
