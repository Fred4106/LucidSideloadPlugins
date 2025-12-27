package com.fred4106.improvedCharges.items.barrows;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.events.CustomMenuOptionClicked;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;

public class _BarrowsItem extends ChargedItem {
    public _BarrowsItem(
            final String itemName,
            final int itemId,
            final Provider provider
            ) {
        super(
            Constants.BARROWS_GEAR + "_" + itemName.toLowerCase().replaceAll("'", "").replaceAll(" ", "_"),
            itemId,
            provider
        );

        this.triggers = new TriggerBase[]{
            // Check.
            new OnChatMessage(itemName + ": (?<percentage>.+)% remaining until the next degradation.").matcherConsumer((m) -> {
                final int percentage = Integer.parseInt(m.group("percentage"));
                final int chargesUsedInCurrentTier = (100 - percentage) * 250 / 100;

                for (final CustomMenuOptionClicked menuOptionClicked : provider.store.menuOptionsClicked) {
                    if (menuOptionClicked.target.contains(provider.itemManager.getItemComposition(itemId).getName())) {
                        final int currentTierMaxCharges = Integer.parseInt(menuOptionClicked.target.replaceAll("\\D", "")) * 10;
                        setCharges(currentTierMaxCharges - chargesUsedInCurrentTier);
                        return;
                    }
                }
            }),

            // Degrade in combat.
            new OnCombat(90).isEquipped().decreaseCharges(1),
        };
    }

    @Override
    public String getChargesString(final int itemId) {
        final int charges = getCharges(itemId);

        switch (provider.config.combatTimeDegradableStyle()) {
            case PERCENTAGE:
                return charges * 100 / 1000 + "%";
            case TIME:
                final double hours = (double) (charges * 90 * 600) / 1000 / 3600;
                return String.format("%.1fh", hours).replaceAll("\\.0", "");
            case CHARGES:
            default:
                return super.getChargesString(itemId);
        }
    }

    @Override
    public String getTotalChargesString() {
        return getChargesString(itemId);
    }

    @Override
    public String getConfigKey() {
        return Constants.BARROWS_GEAR;
    }
}
