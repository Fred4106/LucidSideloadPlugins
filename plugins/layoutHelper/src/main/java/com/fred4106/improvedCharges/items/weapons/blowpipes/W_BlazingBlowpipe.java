package com.fred4106.improvedCharges.items.weapons.blowpipes;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class W_BlazingBlowpipe extends W_ToxicBlowpipe {
    public W_BlazingBlowpipe(Provider provider) {
        super(provider, FredsItemChargesConfig.blazing_blowpipe, ItemID.TOXIC_BLOWPIPE_ORNAMENT, new TriggerItem[]{
            new TriggerItem(ItemID.TOXIC_BLOWPIPE_ORNAMENT).fixedCharges(0),
            new TriggerItem(ItemID.TOXIC_BLOWPIPE_LOADED_ORNAMENT),
        });
    }
}
