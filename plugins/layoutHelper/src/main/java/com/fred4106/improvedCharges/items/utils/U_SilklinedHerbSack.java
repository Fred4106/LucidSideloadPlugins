package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.*;

public class U_SilklinedHerbSack extends U_HerbSack {
    public U_SilklinedHerbSack(Provider provider) {
        super(FredsItemChargesConfig.silklined_herb_sack, ItemID.SLAYER_HERB_SACK_SILK, ItemID.SLAYER_HERB_SACK_SILK_OPEN, 100, provider);
    }
}