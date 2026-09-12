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

public class S_TomeOfFire extends ChargedItem {
    public S_TomeOfFire(Provider provider) {
        super(FredsItemChargesConfig.tome_of_fire, ItemID.TOME_OF_FIRE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.TOME_OF_FIRE_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemID.TOME_OF_FIRE).needsToBeEquipped(),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your tome has been charged with (Burnt|Searing) Pages. It currently holds (?<charges>.+) charges?.").setDynamicallyCharges().onItemClick(),

            // Attack with regular spellbook fire spells.
            new OnGraphicChanged(99, 126, 129, 155, 1464).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnAutoChargeMessage("Tome of Fire", "Burnt page", 20, this)
        ));
    }
}
