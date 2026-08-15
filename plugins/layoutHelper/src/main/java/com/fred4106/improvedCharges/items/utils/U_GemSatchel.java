package com.fred4106.improvedCharges.items.utils;

import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.Provider;

public class U_GemSatchel extends U_AbstractGemContainer {
    public U_GemSatchel(Provider provider) {
        super(
            FredsItemChargesConfig.gem_satchel,
            ItemID.GEM_SATCHEL,
            ItemID.GEM_SATCHEL_OPEN,
            10,
            "gem satchel",
            false,
            true,
            provider
        );
    }
}
