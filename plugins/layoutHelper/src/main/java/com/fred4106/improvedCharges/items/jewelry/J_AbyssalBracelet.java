package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_AbyssalBracelet extends ChargedItem {
    public J_AbyssalBracelet(Provider provider) {
        super(FredsItemChargesConfig.abyssal_bracelet, ItemID.JEWL_RUNERUNNING_BRACELET_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.JEWL_RUNERUNNING_BRACELET_1).fixedCharges(1),
            new TriggerItem(ItemID.JEWL_RUNERUNNING_BRACELET_2).fixedCharges(2),
            new TriggerItem(ItemID.JEWL_RUNERUNNING_BRACELET_3).fixedCharges(3),
            new TriggerItem(ItemID.JEWL_RUNERUNNING_BRACELET_4).fixedCharges(4),
            new TriggerItem(ItemID.JEWL_RUNERUNNING_BRACELET_5).fixedCharges(5),
        };
    }
}
