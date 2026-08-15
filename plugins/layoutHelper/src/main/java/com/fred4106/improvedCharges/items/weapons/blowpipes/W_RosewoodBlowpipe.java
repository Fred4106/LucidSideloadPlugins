package com.fred4106.improvedCharges.items.weapons.blowpipes;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.ids.*;

public class W_RosewoodBlowpipe extends _Blowpipe {
    public W_RosewoodBlowpipe(Provider provider) {
        super(
            FredsItemChargesConfig.rosewood_blowpipe,
            ItemID.ROSEWOOD_BLOWPIPE,
            provider,
            new TriggerItem[]{
                new TriggerItem(ItemID.ROSEWOOD_BLOWPIPE),
                new TriggerItem(ItemID.ROSEWOOD_BLOWPIPE_EMPTY).fixedCharges(0)
            },
            true,
            true,
            13144
        );
    }
}
