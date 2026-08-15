package com.fred4106.improvedCharges.items.helms;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class H_MagmaHelm extends H_SerpentineHelm {
    public H_MagmaHelm(Provider provider) {
        super(FredsItemChargesConfig.magma_helm, "Magma helm", ItemID.SERPENTINE_HELM_CHARGED_RED, new TriggerItem[]{
            new TriggerItem(ItemID.SERPENTINE_HELM_RED).fixedCharges(0),
            new TriggerItem(ItemID.SERPENTINE_HELM_CHARGED_RED)
        }, provider);
    }
}
