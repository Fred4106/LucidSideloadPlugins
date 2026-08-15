package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_ZamorakMix extends _Potion {
    public P_ZamorakMix(Provider provider) {
        super("zamorak_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSEPOTIONOFZAMORAK).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSEPOTIONOFZAMORAK).fixedCharges(2),
        }, provider);
    }
}
