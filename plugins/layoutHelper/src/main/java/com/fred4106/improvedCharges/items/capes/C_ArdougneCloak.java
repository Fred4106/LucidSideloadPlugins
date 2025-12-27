package com.fred4106.improvedCharges.items.capes;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.Provider;

public class C_ArdougneCloak extends ChargedItem {
    public C_ArdougneCloak(final Provider provider) {
        super(Constants.ARDOUGNE_CLOAK, ItemId.ARDOUGNE_CLOAK_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ARDOUGNE_CLOAK_1).fixedCharges(ChargeId.UNLIMITED),
            new TriggerItem(ItemId.ARDOUGNE_CLOAK_2),
            new TriggerItem(ItemId.ARDOUGNE_CLOAK_3),
            new TriggerItem(ItemId.ARDOUGNE_CLOAK_4).fixedCharges(ChargeId.UNLIMITED),
        };

        this.triggers = new TriggerBase[] {
            new OnChatMessage("You have used (?<used>.+) of your (?<total>.+) Ardougne Farm teleports for today.").setDifferenceCharges(),
            new OnResetDaily().specificItem(ItemId.ARDOUGNE_CLOAK_2).setFixedCharges(3),
            new OnResetDaily().specificItem(ItemId.ARDOUGNE_CLOAK_3).setFixedCharges(5),
        };
    }
}
