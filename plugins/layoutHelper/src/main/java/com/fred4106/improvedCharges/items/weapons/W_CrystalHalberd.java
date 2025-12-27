package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.store.ids.AnimationId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import static com.fred4106.improvedCharges.store.enums.HitsplatTarget.ENEMY;

public class W_CrystalHalberd extends ChargedItem {
    public W_CrystalHalberd(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.CRYSTAL_HALBERD, ItemId.CRYSTAL_HALBERD, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CRYSTAL_HALBERD_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_HALBERD),
            new TriggerItem(ItemId.CRYSTAL_HALBERD_FULL).fixedCharges(2500),
        };
        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("Your crystal halberd has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Attack with stab.
            new OnAnimationChanged(AnimationId.HUMAN_SPEAR_SPIKE).isEquipped().decreaseCharges(1),

            // Attack with slash.
            new OnAnimationChanged(AnimationId.HUMAN_SCYTHE_SWEEP).isEquipped().decreaseCharges(1),
        };
    }
}
