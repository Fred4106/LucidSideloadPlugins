package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_AmuletOfGlory extends ChargedItem {
    public J_AmuletOfGlory(Provider provider) {
        super(FredsItemChargesConfig.amulet_of_glory, ItemID.AMULET_OF_GLORY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.AMULET_OF_GLORY).fixedCharges(0),
            new TriggerItem(ItemID.AMULET_OF_GLORY_1).fixedCharges(1),
            new TriggerItem(ItemID.AMULET_OF_GLORY_2).fixedCharges(2),
            new TriggerItem(ItemID.AMULET_OF_GLORY_3).fixedCharges(3),
            new TriggerItem(ItemID.AMULET_OF_GLORY_4).fixedCharges(4),
            new TriggerItem(ItemID.AMULET_OF_GLORY_5).fixedCharges(5),
            new TriggerItem(ItemID.AMULET_OF_GLORY_6).fixedCharges(6),
            new TriggerItem(ItemID.AMULET_OF_GLORY_INF).unlimitedCharges(),
            new TriggerItem(ItemID.TRAIL_AMULET_OF_GLORY).fixedCharges(0),
            new TriggerItem(ItemID.TRAIL_AMULET_OF_GLORY_1).fixedCharges(1),
            new TriggerItem(ItemID.TRAIL_AMULET_OF_GLORY_2).fixedCharges(2),
            new TriggerItem(ItemID.TRAIL_AMULET_OF_GLORY_3).fixedCharges(3),
            new TriggerItem(ItemID.TRAIL_AMULET_OF_GLORY_4).fixedCharges(4),
            new TriggerItem(ItemID.TRAIL_AMULET_OF_GLORY_5).fixedCharges(5),
            new TriggerItem(ItemID.TRAIL_AMULET_OF_GLORY_6).fixedCharges(6),
        };
    }
}
