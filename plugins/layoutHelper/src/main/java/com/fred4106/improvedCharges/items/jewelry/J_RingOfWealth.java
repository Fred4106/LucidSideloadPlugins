package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_RingOfWealth extends ChargedItem {
    public J_RingOfWealth(Provider provider) {
        super(FredsItemChargesConfig.ring_of_wealth, ItemID.RING_OF_WEALTH, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.RING_OF_WEALTH).fixedCharges(0),
            new TriggerItem(ItemID.RING_OF_WEALTH_1).fixedCharges(1),
            new TriggerItem(ItemID.RING_OF_WEALTH_2).fixedCharges(2),
            new TriggerItem(ItemID.RING_OF_WEALTH_3).fixedCharges(3),
            new TriggerItem(ItemID.RING_OF_WEALTH_4).fixedCharges(4),
            new TriggerItem(ItemID.RING_OF_WEALTH_5).fixedCharges(5),
            new TriggerItem(ItemID.RING_OF_WEALTH_I).fixedCharges(0),
            new TriggerItem(ItemID.RING_OF_WEALTH_I1).fixedCharges(1),
            new TriggerItem(ItemID.RING_OF_WEALTH_I2).fixedCharges(2),
            new TriggerItem(ItemID.RING_OF_WEALTH_I3).fixedCharges(3),
            new TriggerItem(ItemID.RING_OF_WEALTH_I4).fixedCharges(4),
            new TriggerItem(ItemID.RING_OF_WEALTH_I5).fixedCharges(5),
        };
    }
}
