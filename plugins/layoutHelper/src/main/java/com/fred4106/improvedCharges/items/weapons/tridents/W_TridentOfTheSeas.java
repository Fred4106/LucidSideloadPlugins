package com.fred4106.improvedCharges.items.weapons.tridents;

import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_TridentOfTheSeas extends _Trident {
    public W_TridentOfTheSeas(Provider provider) {
        super(
            FredsItemChargesConfig.trident_of_the_seas,
            ItemID.TOTS_CHARGED,
            ItemID.TOTS_UNCHARGED,
            Optional.of(ItemID.TOTS),
            "Trident of the Seas",
            1251,
            provider
        );
    }
}