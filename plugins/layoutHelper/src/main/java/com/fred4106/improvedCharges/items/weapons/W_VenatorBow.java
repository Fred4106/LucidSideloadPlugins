package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class W_VenatorBow extends ChargedItem {
    public W_VenatorBow(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.VENATOR_BOW, ItemId.VENATOR_BOW, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.VENATOR_BOW_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.VENATOR_BOW)
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your venator bow has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Attack.
            new OnGraphicChanged(2289).decreaseCharges(1)
        ));
    }
}
