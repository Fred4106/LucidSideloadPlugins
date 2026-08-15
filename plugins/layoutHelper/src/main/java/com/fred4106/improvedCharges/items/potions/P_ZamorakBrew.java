package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.*;

public class P_ZamorakBrew extends _Potion {
    public P_ZamorakBrew(Provider provider) {
        super("zamorak_brew", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEPOTIONOFZAMORAK).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEPOTIONOFZAMORAK).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEPOTIONOFZAMORAK).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEPOTIONOFZAMORAK).fixedCharges(4),
        }, provider);
    }
}
