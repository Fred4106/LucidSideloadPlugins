package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class W_IbansStaff extends ChargedItem {
    public W_IbansStaff(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.IBANS_STAFF, ItemId.IBANS_STAFF, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.IBANS_STAFF),
            new TriggerItem(ItemId.IBANS_STAFF_BROKEN),
            new TriggerItem(ItemId.IBANS_STAFF_UPGRADED),
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("You have (?<charges>.+) charges left on the staff.").setDynamicallyCharges().onItemClick(),

            // Attack.
            new OnGraphicChanged(87).isEquipped().decreaseCharges(1),
        };
    }
}
