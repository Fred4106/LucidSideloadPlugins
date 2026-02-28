package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class J_SkillsNecklace extends ChargedItem {
    public J_SkillsNecklace(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.SKILLS_NECKLACE, ItemId.SKILLS_NECKLACE_0, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.SKILLS_NECKLACE_0).fixedCharges(0),
            new TriggerItem(ItemId.SKILLS_NECKLACE_1).fixedCharges(1),
            new TriggerItem(ItemId.SKILLS_NECKLACE_2).fixedCharges(2),
            new TriggerItem(ItemId.SKILLS_NECKLACE_3).fixedCharges(3),
            new TriggerItem(ItemId.SKILLS_NECKLACE_4).fixedCharges(4),
            new TriggerItem(ItemId.SKILLS_NECKLACE_5).fixedCharges(5),
            new TriggerItem(ItemId.SKILLS_NECKLACE_6).fixedCharges(6),
        };

        this.triggers.addAll(List.of(
            // Unified menu entry.
            new OnMenuEntryAdded("Rub").replaceOption("Teleport")
        ));
    }
}
