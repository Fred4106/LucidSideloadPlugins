package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class S_TomeOfEarth extends ChargedItem {
    public S_TomeOfEarth(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.TOME_OF_EARTH, ItemId.TOME_OF_EARTH, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TOME_OF_EARTH_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.TOME_OF_EARTH).needsToBeEquipped(),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your tome currently holds (?<charges>.+) charges?.").setDynamicallyCharges().onItemClick(),

            // Attack with regular spellbook earth spells.
            new OnGraphicChanged(96, 123, 138, 164, 1461).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Tome of earth using (?<soiledpage>.+)x Soiled page.").matcherConsumer(m -> {
                final int soiledPages = Integer.parseInt(m.group("soiledpage"));
                increaseCharges(soiledPages * 20);
            })
        ));
    }
}
