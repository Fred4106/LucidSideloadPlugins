package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class U_CrystalSaw extends ChargedItem {
    public U_CrystalSaw(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.CRYSTAL_SAW, ItemId.CRYSTAL_SAW, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CRYSTAL_SAW),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your saw has (?<charges>.+) charges? left.").setDynamicallyCharges()
        ));
    }
}
