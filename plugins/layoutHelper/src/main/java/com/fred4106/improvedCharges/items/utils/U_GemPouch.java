package com.fred4106.improvedCharges.items.utils;

import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.Provider;

public class U_GemPouch extends U_AbstractGemContainer {
    public U_GemPouch(Provider provider) {
        super(
            FredsItemChargesConfig.gem_pouch,
            ItemID.GEM_POUCH,
            ItemID.GEM_POUCH_OPEN,
            5,
            "gem pouch",
            false,
            true,
            provider
        );
    }
}
