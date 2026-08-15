package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_RestoreMix extends _Potion {
    public P_RestoreMix(Provider provider) {
        super("restore_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSESTATRESTORE).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSESTATRESTORE).fixedCharges(2),
        }, provider);
    }
}
