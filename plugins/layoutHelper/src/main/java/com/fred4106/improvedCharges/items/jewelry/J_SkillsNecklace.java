package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class J_SkillsNecklace extends ChargedItem {
    public J_SkillsNecklace(Provider provider) {
        super(FredsItemChargesConfig.skills_necklace, ItemID.JEWL_NECKLACE_OF_SKILLS, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.JEWL_NECKLACE_OF_SKILLS).fixedCharges(0),
            new TriggerItem(ItemID.JEWL_NECKLACE_OF_SKILLS_1).fixedCharges(1),
            new TriggerItem(ItemID.JEWL_NECKLACE_OF_SKILLS_2).fixedCharges(2),
            new TriggerItem(ItemID.JEWL_NECKLACE_OF_SKILLS_3).fixedCharges(3),
            new TriggerItem(ItemID.JEWL_NECKLACE_OF_SKILLS_4).fixedCharges(4),
            new TriggerItem(ItemID.JEWL_NECKLACE_OF_SKILLS_5).fixedCharges(5),
            new TriggerItem(ItemID.JEWL_NECKLACE_OF_SKILLS_6).fixedCharges(6),
        };

        this.triggers.addAll(List.of(
            // Unified menu entry.
            new OnMenuEntryAdded("Rub").replaceOption("Teleport")
        ));
    }
}
