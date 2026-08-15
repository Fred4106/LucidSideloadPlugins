package com.fred4106.improvedCharges.items.capes;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.List;

public class C_ArdougneCloak extends ChargedItem {
    public C_ArdougneCloak(Provider provider) {
        super(FredsItemChargesConfig.ardougne_cloak, ItemID.ARDY_CAPE_EASY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.ARDY_CAPE_EASY).unlimitedCharges(),
            new TriggerItem(ItemID.ARDY_CAPE_MEDIUM),
            new TriggerItem(ItemID.ARDY_CAPE_HARD),
            new TriggerItem(ItemID.ARDY_CAPE_ELITE).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("You have used (?<used>.+) of your (?<total>.+) Ardougne Farm teleports for today.").setDifferenceCharges(),
            new OnResetDaily().specificItem(ItemID.ARDY_CAPE_MEDIUM).setFixedCharges(3),
            new OnResetDaily().specificItem(ItemID.ARDY_CAPE_HARD).setFixedCharges(5)
        ));
    }
}
