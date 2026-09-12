package com.fred4106.improvedCharges.items.weapons.tridents;

import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_TridentOfTheSeasE extends _Trident {
    public W_TridentOfTheSeasE(Provider provider) {
        super(
            FredsItemChargesConfig.trident_of_the_seas_e,
            ItemID.TOTS_I_CHARGED,
            ItemID.TOTS_I_UNCHARGED,
            Optional.empty(),
            "Trident of the Seas (e)",
            1251,
            provider
        );
    }
}
