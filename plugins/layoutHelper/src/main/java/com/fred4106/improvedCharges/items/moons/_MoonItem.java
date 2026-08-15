package com.fred4106.improvedCharges.items.moons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnCombat;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;
import java.util.regex.*;

public class _MoonItem extends ChargedItem {
    public _MoonItem(String checkName, int itemId, Provider provider) {
        super(FredsItemChargesConfig.moons_gear + "_" + checkName.toLowerCase().replaceAll("\\s", "_"), itemId, provider);

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your " + Pattern.quote(checkName) + "( only)? has (?<charges>.+) charges? (remaining|left).").setDynamicallyCharges(),

            // In combat.
            new OnCombat(90).isEquipped().decreaseCharges(1)
        ));
    }

    @Override
    public String getChargesString(int itemId) {
        return getLongChargesString(itemId);
    }

    @Override
    public String getLongChargesString(int itemId) {
        int charges = getCharges(itemId);

        switch (provider.config.combatTimeDegradableStyle()) {
            case PERCENTAGE:
                return charges * 100 / 3000 + "%";
            case TIME:
                double hours = (double) (charges * 90 * 600) / 1000 / 3600;
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
        return FredsItemChargesConfig.moons_gear;
    }
}
