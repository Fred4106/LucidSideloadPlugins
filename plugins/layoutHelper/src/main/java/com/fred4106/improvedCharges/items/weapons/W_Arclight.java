package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.ids.AnimationId;
import com.fred4106.improvedCharges.store.ids.GraphicId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import static com.fred4106.improvedCharges.store.enums.HitsplatTarget.ENEMY;

public class W_Arclight extends ChargedItem {
    private boolean attacked = false;

    public W_Arclight(final Provider provider) {
        super(Constants.ARCLIGHT, ItemId.ARCLIGHT, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ARCLIGHT),
            new TriggerItem(ItemId.ARCLIGHT_UNCHARGED).fixedCharges(0),
        };

        this.triggers = new TriggerBase[] {
            new OnChatMessage("Your arclight has (?<charges>.+) charges?( left)?.").setDynamicallyCharges(),
            new OnChatMessage("Your arclight can perform (?<charges>.+) more attacks.").setDynamicallyCharges(),
            new OnChatMessage("Your arclight has degraded.").setFixedCharges(0),

            // Attack
            new OnAnimationChanged(AnimationId.HUMAN_SWORD_SLASH, AnimationId.HUMAN_SWORD_STAB).isEquipped().decreaseCharges(1).consumer(() -> {
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
            }),
        };
    }
}
