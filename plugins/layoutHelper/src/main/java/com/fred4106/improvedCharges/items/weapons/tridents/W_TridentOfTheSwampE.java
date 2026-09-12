package com.fred4106.improvedCharges.items.weapons.tridents;

import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_TridentOfTheSwampE extends _Trident {
    public W_TridentOfTheSwampE(Provider provider) {
        super(
            FredsItemChargesConfig.trident_of_the_swamp_e,
            ItemID.TOXIC_TOTS_I_CHARGED,
            ItemID.TOXIC_TOTS_I_UNCHARGED,
            Optional.empty(),
            "Trident of the Swamp (e)",
            665,
            provider
        );
    }
}
