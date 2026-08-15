package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_PhoenixNecklace extends ChargedItem {
    public J_PhoenixNecklace(Provider provider) {
        super(FredsItemChargesConfig.phoenix_necklace, ItemID.JEWL_NECKLACE_OF_PHOENIX, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.JEWL_NECKLACE_OF_PHOENIX).fixedCharges(1).needsToBeEquipped(),
        };
    }
}
