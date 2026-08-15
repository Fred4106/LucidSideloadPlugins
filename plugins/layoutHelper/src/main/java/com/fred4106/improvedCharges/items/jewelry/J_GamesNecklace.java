package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_GamesNecklace extends ChargedItem {
    public J_GamesNecklace(Provider provider) {
        super(FredsItemChargesConfig.games_necklace, ItemID.NECKLACE_OF_MINIGAMES_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.NECKLACE_OF_MINIGAMES_1).fixedCharges(1),
            new TriggerItem(ItemID.NECKLACE_OF_MINIGAMES_2).fixedCharges(2),
            new TriggerItem(ItemID.NECKLACE_OF_MINIGAMES_3).fixedCharges(3),
            new TriggerItem(ItemID.NECKLACE_OF_MINIGAMES_4).fixedCharges(4),
            new TriggerItem(ItemID.NECKLACE_OF_MINIGAMES_5).fixedCharges(5),
            new TriggerItem(ItemID.NECKLACE_OF_MINIGAMES_6).fixedCharges(6),
            new TriggerItem(ItemID.NECKLACE_OF_MINIGAMES_7).fixedCharges(7),
            new TriggerItem(ItemID.NECKLACE_OF_MINIGAMES_8).fixedCharges(8),
        };
    }
}
