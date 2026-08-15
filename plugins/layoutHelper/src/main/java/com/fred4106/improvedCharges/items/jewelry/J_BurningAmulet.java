package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_BurningAmulet extends ChargedItem {
    public J_BurningAmulet(
        Provider provider
    ) {
        super(FredsItemChargesConfig.burning_amulet, ItemID.BURNING_AMULET_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BURNING_AMULET_1).fixedCharges(1),
            new TriggerItem(ItemID.BURNING_AMULET_2).fixedCharges(2),
            new TriggerItem(ItemID.BURNING_AMULET_3).fixedCharges(3),
            new TriggerItem(ItemID.BURNING_AMULET_4).fixedCharges(4),
            new TriggerItem(ItemID.BURNING_AMULET_5).fixedCharges(5),
        };

        this.triggers.addAll(List.of(
            new OnMenuEntryAdded("Rub").replaceOption("Teleport")
        ));
    }
}