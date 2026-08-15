package com.fred4106.improvedCharges.items.helms;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class H_TanzaniteHelm extends H_SerpentineHelm {
    public H_TanzaniteHelm(Provider provider) {
        super(FredsItemChargesConfig.tanzanite_helm, "Tanzanite helm", ItemID.SERPENTINE_HELM_CHARGED_CYAN, new TriggerItem[]{
            new TriggerItem(ItemID.SERPENTINE_HELM_CYAN).fixedCharges(0),
            new TriggerItem(ItemID.SERPENTINE_HELM_CHARGED_CYAN)
        }, provider);
    }
}
