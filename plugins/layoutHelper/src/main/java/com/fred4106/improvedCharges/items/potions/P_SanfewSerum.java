package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_SanfewSerum extends _Potion {
    public P_SanfewSerum(Provider provider) {
        super("sanfew_serum", new TriggerItem[]{
            new TriggerItem(ItemID.SANFEW_SALVE_1_DOSE).fixedCharges(1),
            new TriggerItem(ItemID.SANFEW_SALVE_2_DOSE).fixedCharges(2),
            new TriggerItem(ItemID.SANFEW_SALVE_3_DOSE).fixedCharges(3),
            new TriggerItem(ItemID.SANFEW_SALVE_4_DOSE).fixedCharges(4),
        }, provider);
    }
}
