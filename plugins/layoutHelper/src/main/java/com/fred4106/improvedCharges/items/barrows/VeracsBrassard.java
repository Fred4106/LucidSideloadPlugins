package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class VeracsBrassard extends _BarrowsItem {
    public VeracsBrassard(Provider provider) {
        super("Verac's body", ItemID.BARROWS_VERAC_BODY, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BARROWS_VERAC_BODY).fixedCharges(1000),
            new TriggerItem(ItemID.BARROWS_VERAC_BODY_100),
            new TriggerItem(ItemID.BARROWS_VERAC_BODY_75),
            new TriggerItem(ItemID.BARROWS_VERAC_BODY_50),
            new TriggerItem(ItemID.BARROWS_VERAC_BODY_25),
            new TriggerItem(ItemID.BARROWS_VERAC_BODY_BROKEN).fixedCharges(0)
        };
    }
}