package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_RingOfReturning extends ChargedItem {
    public J_RingOfReturning(Provider provider) {
        super(FredsItemChargesConfig.ring_of_returning, ItemID.RING_OF_RETURNING_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.RING_OF_RETURNING_1).fixedCharges(1),
            new TriggerItem(ItemID.RING_OF_RETURNING_2).fixedCharges(2),
            new TriggerItem(ItemID.RING_OF_RETURNING_3).fixedCharges(3),
            new TriggerItem(ItemID.RING_OF_RETURNING_4).fixedCharges(4),
            new TriggerItem(ItemID.RING_OF_RETURNING_5).fixedCharges(5),
        };
    }
}
