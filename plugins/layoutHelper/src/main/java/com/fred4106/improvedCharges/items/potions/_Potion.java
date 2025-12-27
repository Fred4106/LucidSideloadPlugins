package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.FredsItemChargesConfig;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.awt.Color;

public class _Potion extends ChargedItem {
    public _Potion(
        final String configKey,
        final TriggerItem[] items,
        final Provider provider
    ) {
        super(FredsItemChargesConfig.potions + "_" + configKey, items[0].itemId, provider);
        this.items = items;
    }

    @Override
    public String getConfigKey() {
        return FredsItemChargesConfig.potions;
    }

    @Override
    public Color getTextColor(final int itemId) {
        for (final TriggerItem triggerItem : items) {
            if (triggerItem.itemId == itemId && triggerItem.fixedCharges.isPresent()) {
                switch (triggerItem.fixedCharges.get()) {
                    case 4:
                        return provider.config.get4DoseColor();
                    case 3:
                        return provider.config.get3DoseColor();
                    case 2:
                        return provider.config.get2DoseColor();
                    case 1:
                        return provider.config.get1DoseColor();
                }
            }
        }

        return super.getTextColor(itemId);
    }
}
