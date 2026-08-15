package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

import java.util.*;

public class U_StrangeOldLockpick extends ChargedItem {
    public U_StrangeOldLockpick(Provider provider) {
        super(FredsItemChargesConfig.strange_old_lockpick, ItemID.STRANGE_OLD_LOCKPICK_FULL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.STRANGE_OLD_LOCKPICK_FULL).fixedCharges(50),
            new TriggerItem(ItemID.STRANGE_OLD_LOCKPICK),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("Your Strange old lockpick( now)? has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),
            new OnChatMessage("The Strange old lockpick crumbles to dust as you use it one last time.")
        ));
    }
}
