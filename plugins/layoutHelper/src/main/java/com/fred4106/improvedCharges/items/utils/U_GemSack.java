package com.fred4106.improvedCharges.items.utils;

import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.Provider;

public class U_GemSack extends U_AbstractGemContainer {
    public U_GemSack(Provider provider) {
        super(
            FredsItemChargesConfig.gem_sack,
            ItemID.GEM_SACK,
            ItemID.GEM_SACK_OPEN,
            60,
            "gem sack",
            true,
            true,
            provider
        );
    }
}