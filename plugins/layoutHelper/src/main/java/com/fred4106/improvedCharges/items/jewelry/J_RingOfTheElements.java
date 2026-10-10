package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.OnVarbitChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_RingOfTheElements extends ChargedItem {
    public J_RingOfTheElements(Provider provider) {
        super(FredsItemChargesConfig.ring_of_the_elements, ItemID.RING_OF_ELEMENTS_CHARGED, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.RING_OF_ELEMENTS_CHARGED),
            new TriggerItem(ItemID.RING_OF_ELEMENTS).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            // Teleport.
            new OnVarbitChanged(VarbitID.RING_OF_THE_ELEMENTS_CHARGES).setDynamically(),

            // Unified menu entry.
            new OnMenuEntryAdded("Rub").replaceOption("Teleport"),

            // Last destination replaced with actual altar.
            new OnMenuEntryAdded("Last Destination").replaceOptionConsumer(() -> {
                switch (provider.client.getVarbitValue(VarbitID.RING_OF_ELEMENTS_LAST_DESTINATION)) {
                    case 1:
                        return "Air Altar";
                    case 2:
                        return "Water Altar";
                    case 3:
                        return "Earth Altar";
                    case 4:
                        return "Fire Altar";
                    default:
                        return "Last Destination";
                }
            })
        ));
    }
}