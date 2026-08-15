package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.enums.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.*;

public class S_CrystalShield extends ChargedItem {
    public S_CrystalShield(Provider provider) {
        super(FredsItemChargesConfig.crystal_shield, ItemID.CRYSTAL_SHIELD, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.CRYSTAL_SHIELD_2500),
            new TriggerItem(ItemID.CRYSTAL_SHIELD),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your crystal shield has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Get hit.
            new OnHitsplatApplied(HitsplatTarget.SELF, HitsplatGroup.SUCCESSFUL).moreThanZeroDamage().isEquipped().decreaseCharges(1)
        ));
    }
}
