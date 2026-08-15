package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_CastleWarsBracelet extends ChargedItem {
    public J_CastleWarsBracelet(Provider provider) {
        super(FredsItemChargesConfig.castle_wars_bracelet, ItemID.JEWL_CASTLEWARS_BRACELET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.JEWL_CASTLEWARS_BRACELET).fixedCharges(1).needsToBeEquipped(),
            new TriggerItem(ItemID.JEWL_CASTLEWARS_BRACELET2).fixedCharges(2).needsToBeEquipped(),
            new TriggerItem(ItemID.JEWL_CASTLEWARS_BRACELET3).fixedCharges(3).needsToBeEquipped(),
        };
    }
}
