package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.*;

public class S_TomeOfWater extends ChargedItem {
    public S_TomeOfWater(Provider provider) {
        super(FredsItemChargesConfig.tome_of_water, ItemID.TOME_OF_WATER, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.TOME_OF_WATER_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemID.TOME_OF_WATER).needsToBeEquipped(),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your tome currently holds (?<charges>.+) charges?.").setDynamicallyCharges().onItemClick(),

            // Attack with regular spellbook water spells.
            new OnGraphicChanged(93, 120, 135, 161, 1458).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnAutoChargeMessage("Tome of water", "Soaked page", 20, this)
        ));
    }
}
