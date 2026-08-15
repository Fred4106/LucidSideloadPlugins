package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_ArmadylBrew extends _Potion {
    public P_ArmadylBrew(Provider provider) {
        super("armadyl_brew", new TriggerItem[]{
            new TriggerItem(ItemID._1DOSEARMADYLBREW).fixedCharges(1),
            new TriggerItem(ItemID._2DOSEARMADYLBREW).fixedCharges(2),
            new TriggerItem(ItemID._3DOSEARMADYLBREW).fixedCharges(3),
            new TriggerItem(ItemID._4DOSEARMADYLBREW).fixedCharges(4),
        }, provider);
    }
}
