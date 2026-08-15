package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class U_JarGenerator extends ChargedItem {
    public U_JarGenerator(Provider provider) {
        super(FredsItemChargesConfig.jar_generator, ItemID.II_JAR_GENERATOR, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.II_JAR_GENERATOR)
        };

        this.triggers.addAll(List.of(
            // Check or use.
            new OnChatMessage("You have (?<charges>.+) charges left in your jar generator.").setDynamicallyCharges(),

            // Crumbles.
            new OnChatMessage("Your jar generator runs out of charges and disappears.").setFixedCharges(100)
        ));
    }
}
