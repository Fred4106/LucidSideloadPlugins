package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class J_NecklaceOfPassage extends ChargedItem {
    public J_NecklaceOfPassage(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.NECKLACE_OF_PASSAGE, ItemId.NECKLACE_OF_PASSAGE_1, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.NECKLACE_OF_PASSAGE_1).fixedCharges(1),
            new TriggerItem(ItemId.NECKLACE_OF_PASSAGE_2).fixedCharges(2),
            new TriggerItem(ItemId.NECKLACE_OF_PASSAGE_3).fixedCharges(3),
            new TriggerItem(ItemId.NECKLACE_OF_PASSAGE_4).fixedCharges(4),
            new TriggerItem(ItemId.NECKLACE_OF_PASSAGE_5).fixedCharges(5),
        };
    }
}
