package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.enums.CombatStyle;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import com.fred4106.improvedCharges.store.ids.AnimationId;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class J_EfaritaysAid extends ChargedItem {
    private boolean attackedVampyre = false;

    public J_EfaritaysAid(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.EFARITAYS_AID, ItemId.EFARITAYS_AID, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.EFARITAYS_AID).needsToBeEquipped()
        };

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage("Your ring has (?<charges>.+) charges? left.").setDynamicallyCharges().onItemClick(),

            // Break.
            new OnChatMessage("The ring shatters. Your next Efaritay's aid ring will start afresh from (?<charges>.+) charges.").setDynamicallyCharges(),

            // Low charges.
            new OnChatMessage("Your ring has 10 charges left.").isEquipped().setFixedCharges(10),

            // Out of charges.
            new OnChatMessage("Your ring crumbles to dust.").setFixedCharges(200),

            // Charges from break dialog.
            new OnWidgetLoaded(219, 1, 0).text("Status: (?<charges>.+) charges? left.").setDynamically().onMenuOption("Break").onMenuTarget("Efaritay's aid"),

            // Attack tier-2 vampyre.
            new OnHitsplatApplied(HitsplatTarget.ENEMY, HitsplatGroup.ALL).hasTargetName(
                // Tier 1
                "Count Draynor",
                "Dessous",
                "Feral Vampyre",
                "Kroy",
                "Vampyre Juvenile",

                // Tier 2
                "Vampyre Juvinate",

                // Tier 3
                "Damien Leucurte",
                "Ranis Drakan",
                "Vanstrom Klause",
                "Vyrewatch Sentinel",
                "Vyrewatch"
            ).isEquipped().decreaseCharges(1).consumer(() -> {
                attackedVampyre = true;
            }),
            new OnAnimationChanged(AnimationId.THRALL_SKELETON, AnimationId.THRALL_GHOST, AnimationId.THRALL_ZOMBIE).actorName("null").isEquipped().consumer(() -> {
                if (attackedVampyre) {
                    increaseCharges(1);
                    attackedVampyre = false;
                }
            }),
        };
    }
}
