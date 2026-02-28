package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

public class W_SanguinestiStaff extends ChargedItem {
    public W_SanguinestiStaff(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.SANGUINESTI_STAFF, ItemId.SANGUINESTI_STAFF, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.SANGUINESTI_STAFF),
            new TriggerItem(ItemId.SANGUINESTI_STAFF_UNCHARGED).fixedCharges(0),
            new TriggerItem(ItemId.SANGUINESTI_STAFF_HOLY),
            new TriggerItem(ItemId.SANGUINESTI_STAFF_HOLY_UNCHARGED).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your (Holy s|S)anguinesti staff has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Charge partially full.
            new OnChatMessage("You apply an additional .+ charges? to your Sanguinesti staff. It now has (?<charges>.+) charges? in total.").setDynamicallyCharges(),

            // Charge empty.
            new OnChatMessage("You apply (?<charges>.+) charges to your Sanguinesti staff.").setDynamicallyCharges(),

            // Auto-charge.
            new OnChatMessage("The banker charges your (Holy s|S)anguinesti staff using (?<bloodrune>.+)x Blood rune.").matcherConsumer(m -> {
                final int bloodRunes = Integer.parseInt(m.group("bloodrune"));
                increaseCharges(bloodRunes / 3);
            })
        ));
    }
}
