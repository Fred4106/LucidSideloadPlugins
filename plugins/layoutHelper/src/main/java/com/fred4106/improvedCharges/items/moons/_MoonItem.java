package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.store.Provider;

public class _MoonItem extends ChargedItem {
    public _MoonItem(final String configKey, final int itemId, final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.MOONS_GEAR + "_" + configKey, itemId, provider);
    }

    @Override
    public String getChargesString(final int itemId) {
        final int charges = getCharges(itemId);

        switch (provider.config.combatTimeDegradableStyle()) {
            case PERCENTAGE:
                return charges * 100 / 3000 + "%";
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
        return com.fred4106.improvedCharges.Constants.MOONS_GEAR;
    }
}
