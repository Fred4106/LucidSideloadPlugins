package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_RingOfDueling extends ChargedItem {
    public J_RingOfDueling(Provider provider) {
        super(FredsItemChargesConfig.ring_of_dueling, ItemID.RING_OF_DUELING_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.RING_OF_DUELING_1).fixedCharges(1),
            new TriggerItem(ItemID.RING_OF_DUELING_2).fixedCharges(2),
            new TriggerItem(ItemID.RING_OF_DUELING_3).fixedCharges(3),
            new TriggerItem(ItemID.RING_OF_DUELING_4).fixedCharges(4),
            new TriggerItem(ItemID.RING_OF_DUELING_5).fixedCharges(5),
            new TriggerItem(ItemID.RING_OF_DUELING_6).fixedCharges(6),
            new TriggerItem(ItemID.RING_OF_DUELING_7).fixedCharges(7),
            new TriggerItem(ItemID.RING_OF_DUELING_8).fixedCharges(8),
        };
    }
}
