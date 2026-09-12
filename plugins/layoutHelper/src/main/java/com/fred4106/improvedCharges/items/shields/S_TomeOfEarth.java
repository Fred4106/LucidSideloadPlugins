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

public class S_TomeOfEarth extends ChargedItem {
    public S_TomeOfEarth(Provider provider) {
        super(FredsItemChargesConfig.tome_of_earth, ItemID.TOME_OF_EARTH, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.TOME_OF_EARTH_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemID.TOME_OF_EARTH).needsToBeEquipped(),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your tome currently holds (?<charges>.+) charges?.").setDynamicallyCharges().onItemClick(),

            // Attack with regular spellbook earth spells.
            new OnGraphicChanged(96, 123, 138, 164, 1461).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnAutoChargeMessage("Tome of Earth", "Soiled page", 20, this)
        ));
    }
}
