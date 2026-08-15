package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.enums.CombatStyle;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.enums.*;

import java.util.*;

public class J_AmuletOfBloodFury extends ChargedItem {
    public J_AmuletOfBloodFury(Provider provider) {
        super(FredsItemChargesConfig.amulet_of_blood_fury, ItemID.BLOOD_AMULET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BLOOD_AMULET),
        };

        this.triggers.addAll(List.of(
            // Creation
            new OnChatMessage("You have successfully created an Amulet of blood fury.").setFixedCharges(10000),

            // Check.
            new OnChatMessage("Your Amulet of blood fury (will work for|can perform) (?<charges>.+) more hits?.").setDynamicallyCharges(),

            // Charge.
            new OnChatMessage("You have successfully added .+ hits? to your Amulet of blood fury. It will now work for (?<charges>.+) more hits?.").setDynamicallyCharges(),

            // Take damage.
            new OnHitsplatApplied(HitsplatTarget.ENEMY, HitsplatGroup.SUCCESSFUL).combatStyle(CombatStyle.MELEE).isEquipped().decreaseCharges(1)
        ));
    }
}
