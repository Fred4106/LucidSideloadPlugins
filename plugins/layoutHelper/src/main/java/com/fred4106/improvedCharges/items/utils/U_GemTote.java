package com.fred4106.improvedCharges.items.utils;

import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.Provider;

public class U_GemTote extends U_AbstractGemContainer {
    public U_GemTote(Provider provider) {
        super(
            FredsItemChargesConfig.gem_tote,
            ItemID.GEM_TOTE,
            ItemID.GEM_TOTE_OPEN,
            20,
            "gem tote",
            false,
            true,
            provider
        );
    }
}

