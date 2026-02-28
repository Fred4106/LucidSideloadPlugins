package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class W_ToxicStaffOfTheDead extends ChargedItem {
    public W_ToxicStaffOfTheDead(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.TOXIC_STAFF_OF_THE_DEAD, ItemId.TOXIC_STAFF_OF_THE_DEAD, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.TOXIC_STAFF_OF_THE_DEAD_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.TOXIC_STAFF_OF_THE_DEAD)
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("Scales: (?<charges>.+)").setDynamicallyCharges()
        ));
    }
}
