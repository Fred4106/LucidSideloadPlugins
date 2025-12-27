package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class W_EyeOfAyak  extends ChargedItem {
    public W_EyeOfAyak(Provider provider) {
        super(com.fred4106.improvedCharges.Constants.EYE_OF_AYAK, ItemId.EYE_OF_AYAK, provider);

        this.items = new TriggerItem[]{
                new TriggerItem(ItemId.EYE_OF_AYAK_UNCHARGED).fixedCharges(0),
                new TriggerItem(ItemId.EYE_OF_AYAK),
        };

        /*
         * Can be charged with either runes or demon tears but not both.
         * TODO? If the Eye is empty or currently charged with demon tears,
         * it will passively gain 10-20 charges upon defeating the Doom of Mokhaiotl.
         * No game message is displayed when this occurs.
         * If the player has multiple Eyes of Ayak in their inventory upon defeating the Doom of Mokhaiotl,
         * only one of them will be charged.
         */

        this.triggers = new TriggerBase[] {
                // Check.
                // Charge.
                new OnChatMessage("The Eye of Ayak had been charged with (runes|demon tears). It currently has (?<charges>.+) charges?").setDynamicallyCharges(),

                // Uncharge.
                new OnChatMessage("You uncharge the Eye of Ayak").setFixedCharges(0),

                // Attack.
                new OnGraphicChanged(12397).decreaseCharges(1),

                //Special attack
                new OnGraphicChanged(12394).decreaseCharges(1),

                // Auto-charge
                new OnChatMessage("The banker charges your Eye of Ayak using (?<deathrune>.+)x Death rune").matcherConsumer(m -> {
                    final int deathRunes = Integer.parseInt(m.group("deathrune"));
                    increaseCharges(deathRunes / 2);
                }),

                // Auto-charge
                new OnChatMessage("The banker charges your Eye of Ayak using (?<charges>.+)x Demon tear").increaseDynamically(),
        };
    }
}
