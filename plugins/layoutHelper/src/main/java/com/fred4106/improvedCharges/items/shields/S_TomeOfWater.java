package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class S_TomeOfWater extends ChargedItem {
    public S_TomeOfWater(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.TOME_OF_WATER, ItemId.TOME_OF_WATER, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TOME_OF_WATER_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.TOME_OF_WATER).needsToBeEquipped(),
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your tome currently holds (?<charges>.+) charges?.").setDynamicallyCharges().onItemClick(),

            // Attack with regular spellbook water spells.
            new OnGraphicChanged(93, 120, 135, 161, 1458).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Tome of fire using (?<soakedpage>.+)x Soaked page.").matcherConsumer(m -> {
                final int soakedPages = Integer.parseInt(m.group("soakedpage"));
                increaseCharges(soakedPages * 20);
            }),
        };
    }
}
