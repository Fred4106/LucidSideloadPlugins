package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_IbansStaff extends ChargedItem {
    public W_IbansStaff(Provider provider) {
        super(FredsItemChargesConfig.ibans_staff, ItemID.IBANSTAFF, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.IBANSTAFF),
            new TriggerItem(ItemID.BROKENIBANSTAFF),
            new TriggerItem(ItemID.IBANSTAFF_UPGRADED),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("You have (?<charges>.+) charges left on the staff.").setDynamicallyCharges().onItemClick(),

            // Attack.
            new OnGraphicChanged(87).isEquipped().decreaseCharges(1)
        ));
    }
}
