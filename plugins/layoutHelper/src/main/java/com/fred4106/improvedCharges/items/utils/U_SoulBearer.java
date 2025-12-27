package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class U_SoulBearer extends ChargedItem {
    public U_SoulBearer(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.SOUL_BEARER, ItemId.SOUL_BEARER, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.SOUL_BEARER),
            new TriggerItem(ItemId.SOUL_BEARER_UNCHARGED).fixedCharges(0),
        };

        this.triggers = new TriggerBase[] {
            // Uncharge.
            new OnChatMessage("You remove the runes from the soul bearer.").setFixedCharges(0),

            // Check.
            new OnChatMessage("(The|Your) soul bearer( now)? has (?<charges>.+) charges.").setDynamicallyCharges(),

            // Check.
            new OnChatMessage("(The|Your) soul bearer( now)? has one charge.").setFixedCharges(1),

            // Charge.
            new OnChatMessage("You add .+ charges? to your soul bearer. It now has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Charge used.
            new OnChatMessage("Your soul bearer carries the ensouled heads to your bank. It has (?<charges>.+) charges? left.").setDynamicallyCharges(),

            // Last charge used.
            new OnChatMessage("Your soul bearer carries the ensouled heads to your bank. It has run out of charges.").setFixedCharges(0),

            // Auto-charge.
            new OnChatMessage("The banker charges your Soul bearer using (?<bloodrune>.+)x Blood rune.*").matcherConsumer(m -> {
                final int bloodRunes = Integer.parseInt(m.group("bloodrune"));
                increaseCharges(bloodRunes);
            }),
        };
    }
}
