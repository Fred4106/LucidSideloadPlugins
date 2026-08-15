package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.Provider;

public class U_GemBag extends U_AbstractGemContainer {
    public U_GemBag(Provider provider) {
        super(
            FredsItemChargesConfig.gem_bag,
            ItemID.GEM_BAG,
            ItemID.GEM_BAG_OPEN,
            60,
            "gem bag",
            true,
            false,
            provider
        );
    }
}
