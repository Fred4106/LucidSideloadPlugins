package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_GamesNecklace extends ChargedItem {
    public J_GamesNecklace(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.GAMES_NECKLACE, ItemId.GAMES_NECKLACE_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.GAMES_NECKLACE_1).fixedCharges(1),
            new TriggerItem(ItemId.GAMES_NECKLACE_2).fixedCharges(2),
            new TriggerItem(ItemId.GAMES_NECKLACE_3).fixedCharges(3),
            new TriggerItem(ItemId.GAMES_NECKLACE_4).fixedCharges(4),
            new TriggerItem(ItemId.GAMES_NECKLACE_5).fixedCharges(5),
            new TriggerItem(ItemId.GAMES_NECKLACE_6).fixedCharges(6),
            new TriggerItem(ItemId.GAMES_NECKLACE_7).fixedCharges(7),
            new TriggerItem(ItemId.GAMES_NECKLACE_8).fixedCharges(8),
        };
    }
}
