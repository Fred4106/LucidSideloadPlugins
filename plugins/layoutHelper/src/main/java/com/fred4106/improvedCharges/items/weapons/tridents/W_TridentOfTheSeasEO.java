package com.fred4106.improvedCharges.items.weapons.tridents;

import net.runelite.api.gameval.ItemID;
import com.fred4106.improvedCharges.FredsItemChargesConfig;
import com.fred4106.improvedCharges.store.Provider;

import java.util.Optional;

public class W_TridentOfTheSeasEO extends _Trident {
    public W_TridentOfTheSeasEO(Provider provider) {
        super(
            FredsItemChargesConfig.trident_of_the_seas_e_o,
            ItemID.TOTS_I_CHARGED_ORN,
            ItemID.TOTS_I_UNCHARGED_ORN,
            Optional.empty(),
            "Trident of the seas (e) (o)",
            3721,
            provider
        );
    }
}
