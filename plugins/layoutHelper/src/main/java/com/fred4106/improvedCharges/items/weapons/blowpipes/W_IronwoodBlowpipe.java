package com.fred4106.improvedCharges.items.weapons.blowpipes;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.ids.*;

public class W_IronwoodBlowpipe extends _Blowpipe {
    public W_IronwoodBlowpipe(Provider provider) {
        super(
            FredsItemChargesConfig.ironwood_blowpipe,
            ItemID.IRONWOOD_BLOWPIPE,
            provider,
            new TriggerItem[]{
                new TriggerItem(ItemID.IRONWOOD_BLOWPIPE),
                new TriggerItem(ItemID.IRONWOOD_BLOWPIPE_EMPTY).fixedCharges(0)
            },
            true,
            false,
            13143
        );
    }
}
