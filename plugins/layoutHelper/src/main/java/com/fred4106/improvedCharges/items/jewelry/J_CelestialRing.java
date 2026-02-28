package com.fred4106.improvedCharges.items.jewelry;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class J_CelestialRing extends ChargedItem {
    public J_CelestialRing(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.CELESTIAL_RING, ItemId.CELESTIAL_RING, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CELESTIAL_RING_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.CELESTIAL_SIGNET_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.CELESTIAL_RING).needsToBeEquipped(),
            new TriggerItem(ItemId.CELESTIAL_SIGNET).needsToBeEquipped()
        };

        this.triggers.addAll(List.of(
            // Charge.
            new OnChatMessage("You add .+ charges? to your Celestial (ring|signet). It now has (?<charges>.+) charges?.").setDynamicallyCharges(),
            new OnChatMessage("You add (?<charges>.+) charges? to your Celestial (ring|signet).").setDynamicallyCharges(),

            // Check.
            new OnChatMessage("Your Celestial (ring|signet) has (?<charges>.+) charges?.").setDynamicallyCharges(),

            // Ran out of charges.
            new OnChatMessage("Your Celestial (ring|signet) has run out of charges.").setFixedCharges(0),

            // Mine.
            new OnChatMessage("You manage to (mine|quarry) some (clay|copper|tin|guardian fragments|guardian essence|tephra|blurite|limestone|iron|silver|coal|sandstone|gold|granite|mithril|lovakite|adamantite|soft clay)( ore)?.").isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnChatMessage("The banker charges your Celestial (ring|signet) using (?<stardust>.+)x Stardust.").matcherConsumer(m -> {
                final int stardust = Integer.parseInt(m.group("stardust"));
                increaseCharges(stardust);
            })
        ));
    }
}