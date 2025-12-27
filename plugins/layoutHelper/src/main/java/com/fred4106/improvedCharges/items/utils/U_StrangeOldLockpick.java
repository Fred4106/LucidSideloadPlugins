package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class U_StrangeOldLockpick extends ChargedItem {
    public U_StrangeOldLockpick(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.STRANGE_OLD_LOCKPICK, ItemId.STRANGE_OLD_LOCKPICK, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.STRANGE_OLD_LOCKPICK).fixedCharges(50),
            new TriggerItem(ItemId.STRANGE_OLD_LOCKPICK_DEGRADED),
        };

        this.triggers = new TriggerBase[] {
            new OnChatMessage("Your Strange old lockpick( now)? has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),
            new OnChatMessage("The Strange old lockpick crumbles to dust as you use it one last time."),
        };
    }
}
