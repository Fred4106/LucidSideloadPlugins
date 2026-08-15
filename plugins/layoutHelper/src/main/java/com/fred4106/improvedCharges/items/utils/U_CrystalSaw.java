package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class U_CrystalSaw extends ChargedItem {
    public U_CrystalSaw(Provider provider) {
        super(FredsItemChargesConfig.crystal_saw, ItemID.EYEGLO_CRYSTAL_SAW, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.EYEGLO_CRYSTAL_SAW),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your saw has (?<charges>.+) charges? left.").setDynamicallyCharges()
        ));
    }
}
