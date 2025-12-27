package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

public class J_RingOfTheElements extends ChargedItem {
    public J_RingOfTheElements(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.RING_OF_THE_ELEMENTS, ItemId.RING_OF_THE_ELEMENTS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.RING_OF_THE_ELEMENTS),
            new TriggerItem(ItemId.RING_OF_THE_ELEMENTS_UNCHARGED).fixedCharges(0),
        };

        this.triggers = new TriggerBase[] {
            // Teleport.
            new OnVarbitChanged(13707).setDynamically(),

            // Unified menu entry.
            new OnMenuEntryAdded("Rub").replaceOption("Teleport"),

            // Last destination replaced with actual altar.
            new OnMenuEntryAdded("Last Destination").replaceOption("Air Altar").replaceTarget("Ring of the elements", "").varbitCheck(13708, 1),
            new OnMenuEntryAdded("Last Destination").replaceOption("Water Altar").replaceTarget("Ring of the elements", "").varbitCheck(13708, 2),
            new OnMenuEntryAdded("Last Destination").replaceOption("Earth Altar").replaceTarget("Ring of the elements", "").varbitCheck(13708, 3),
            new OnMenuEntryAdded("Last Destination").replaceOption("Fire Altar").replaceTarget("Ring of the elements", "").varbitCheck(13708, 4),
        };
    }
}