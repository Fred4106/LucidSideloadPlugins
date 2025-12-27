package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class S_TomeOfFire extends ChargedItem {
    public S_TomeOfFire(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.TOME_OF_FIRE, ItemId.TOME_OF_FIRE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TOME_OF_FIRE_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.TOME_OF_FIRE).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your tome has been charged with (Burnt|Searing) Pages. It currently holds (?<charges>.+) charges?.").setDynamicallyCharges().onItemClick(),

            // Attack with regular spellbook fire spells.
            new OnGraphicChanged(99, 126, 129, 155, 1464).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Tome of fire using (?<burntpage>.+)x Burnt page.").matcherConsumer(m -> {
                final int burntPages = Integer.parseInt(m.group("burntpage"));
                increaseCharges(burntPages * 20);
            }),
        };
    }
}
