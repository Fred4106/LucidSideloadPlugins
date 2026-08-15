package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.enums.*;
import com.fred4106.improvedCharges.store.ids.*;

import java.util.*;

import static com.fred4106.improvedCharges.store.enums.HitsplatTarget.*;

public class W_Arclight extends ChargedItem {
    private boolean attacked = false;

    public W_Arclight(Provider provider) {
        super(FredsItemChargesConfig.arclight, ItemID.ARCLIGHT, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.ARCLIGHT),
            new TriggerItem(ItemID.ARCLIGHT_INACTIVE).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("Your arclight has (?<charges>.+) charges?( left)?.").setDynamicallyCharges(),
            new OnChatMessage("Your arclight can perform (?<charges>.+) more attacks.").setDynamicallyCharges(),
            new OnChatMessage("Your arclight has degraded.").setFixedCharges(0),

            // Attack
            new OnAnimationChanged(AnimationID.HUMAN_SWORD_SLASH, AnimationID.HUMAN_SWORD_STAB).isEquipped().decreaseCharges(1).consumer(() -> {
                attacked = true;
            }),
            new OnHitsplatApplied(ENEMY, HitsplatGroup.BLOCKED).isEquipped().consumer(() -> {
                if (attacked) {
                    increaseCharges(1);
                    attacked = false;
                }
            }),
            new OnHitsplatApplied(ENEMY, HitsplatGroup.SUCCESSFUL).isEquipped().consumer(() -> {
                attacked = false;
            })
        ));
    }
}
