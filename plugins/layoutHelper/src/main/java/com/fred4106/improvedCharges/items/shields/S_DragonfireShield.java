package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class S_DragonfireShield extends ChargedItem {
    public S_DragonfireShield(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.DRAGONFIRE_SHIELD, ItemId.DRAGONFIRE_SHIELD, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.DRAGONFIRE_SHIELD_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.DRAGONFIRE_SHIELD),
            new TriggerItem(ItemId.DRAGONFIRE_WARD_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.DRAGONFIRE_WARD)
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("The shield has (?<charges>.+) charges?.").setDynamicallyCharges().onItemClick(),

            // Uncharge.
            new OnChatMessage("You vent the shield's remaining charges harmlessly into the air.").setFixedCharges(0),

            // Charge collected.
            new OnChatMessage("Your dragonfire (shield|ward) glows more brightly.").increaseCharges(1),

            // Already full.
            new OnChatMessage("Your dragonfire shield is already fully charged.").setFixedCharges(50),

            // Attack.
            new OnGraphicChanged(1165).isEquipped().decreaseCharges(1),
        };
    }
}
