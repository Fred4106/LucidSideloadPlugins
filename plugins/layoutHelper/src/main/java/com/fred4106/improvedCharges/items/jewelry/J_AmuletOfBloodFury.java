package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.enums.CombatStyle;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class J_AmuletOfBloodFury extends ChargedItem {
    public J_AmuletOfBloodFury(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.AMULET_OF_BLOOD_FURY, ItemId.AMULET_OF_BLOOD_FURY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.AMULET_OF_BLOOD_FURY),
        };

        this.triggers = new TriggerBase[]{
            // Creation
            new OnChatMessage("You have successfully created an Amulet of blood fury.").setFixedCharges(10000),

            // Check.
            new OnChatMessage("Your Amulet of blood fury (will work for|can perform) (?<charges>.+) more hits?.").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You have successfully added .+ hits? to your Amulet of blood fury. It will now work for (?<charges>.+) more hits?.").setDynamicallyCharges(),

            // Take damage.
            new OnHitsplatApplied(HitsplatTarget.ENEMY, HitsplatGroup.SUCCESSFUL).combatStyle(CombatStyle.MELEE).isEquipped().decreaseCharges(1),
        };
    }
}
