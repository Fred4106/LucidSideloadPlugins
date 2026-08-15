package com.fred4106.improvedCharges.items.weapons.tridents;

import net.runelite.api.gameval.ItemID;
import com.fred4106.improvedCharges.FredsItemChargesConfig;
import com.fred4106.improvedCharges.store.Provider;

import java.util.Optional;

public class W_TridentOfTheSwampO extends _Trident {
    public W_TridentOfTheSwampO(Provider provider) {
        super(
            FredsItemChargesConfig.trident_of_the_swamp_o,
            ItemID.TOXIC_TOTS_CHARGED_ORN,
            ItemID.TOXIC_TOTS_UNCHARGED_ORN,
            Optional.empty(),
            "Trident of the swamp (o)",
            3722,
            provider
        );
    }
}
