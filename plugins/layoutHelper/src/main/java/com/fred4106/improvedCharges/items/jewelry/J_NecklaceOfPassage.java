package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class J_NecklaceOfPassage extends ChargedItem {
    public J_NecklaceOfPassage(Provider provider) {
        super(FredsItemChargesConfig.necklace_of_passage, ItemID.NECKLACE_OF_PASSAGE_1, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.NECKLACE_OF_PASSAGE_1).fixedCharges(1),
            new TriggerItem(ItemID.NECKLACE_OF_PASSAGE_2).fixedCharges(2),
            new TriggerItem(ItemID.NECKLACE_OF_PASSAGE_3).fixedCharges(3),
            new TriggerItem(ItemID.NECKLACE_OF_PASSAGE_4).fixedCharges(4),
            new TriggerItem(ItemID.NECKLACE_OF_PASSAGE_5).fixedCharges(5),
        };
    }
}
