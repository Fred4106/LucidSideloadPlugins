package com.fred4106.improvedCharges.items.weapons.blowpipes;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class W_CamphorBlowpipe extends _Blowpipe {
    public W_CamphorBlowpipe(Provider provider) {
        super(
            FredsItemChargesConfig.camphor_blowpipe,
            ItemID.CAMPHOR_BLOWPIPE,
            provider,
            new TriggerItem[]{
                new TriggerItem(ItemID.CAMPHOR_BLOWPIPE),
                new TriggerItem(ItemID.CAMPHOR_BLOWPIPE_EMPTY).fixedCharges(0)
            },
            false,
            false,
            13142
        );
    }
}
