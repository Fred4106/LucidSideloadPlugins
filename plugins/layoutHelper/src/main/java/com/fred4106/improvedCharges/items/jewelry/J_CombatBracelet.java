package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_CombatBracelet extends ChargedItem {
    public J_CombatBracelet(Provider provider) {
        super(FredsItemChargesConfig.combat_bracelet, ItemID.JEWL_BRACELET_OF_COMBAT, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.JEWL_BRACELET_OF_COMBAT).fixedCharges(0),
            new TriggerItem(ItemID.JEWL_BRACELET_OF_COMBAT_1).fixedCharges(1),
            new TriggerItem(ItemID.JEWL_BRACELET_OF_COMBAT_2).fixedCharges(2),
            new TriggerItem(ItemID.JEWL_BRACELET_OF_COMBAT_3).fixedCharges(3),
            new TriggerItem(ItemID.JEWL_BRACELET_OF_COMBAT_4).fixedCharges(4),
            new TriggerItem(ItemID.JEWL_BRACELET_OF_COMBAT_5).fixedCharges(5),
            new TriggerItem(ItemID.JEWL_BRACELET_OF_COMBAT_6).fixedCharges(6),
        };
    }
}
