package com.fred4106.improvedCharges.items.weapons.venator;

import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.*;

public class W_VenatorBow extends _VenatorBow {
    public W_VenatorBow(Provider provider) {
        super(FredsItemChargesConfig.venator_bow, ItemID.VENATOR_BOW, ItemID.VENATOR_BOW_UNCHARGED, "Venator bow", provider);
    }
}
