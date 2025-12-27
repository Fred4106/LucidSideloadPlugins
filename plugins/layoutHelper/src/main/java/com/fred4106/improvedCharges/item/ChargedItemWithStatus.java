package com.fred4106.improvedCharges.item;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.store.Provider;

import java.awt.Color;
import java.util.Optional;

public class ChargedItemWithStatus extends ChargedItem {
    public ChargedItemWithStatus(String configKey, int itemId, final Provider provider) {
        super(configKey, itemId, provider);
    }

    public boolean isDeactivated() {
        final Optional<String> status = Optional.ofNullable(provider.configManager.getConfiguration(Constants.GROUP, getConfigStatusKey()));

        if (!status.isPresent()) {
            return false;
        }

        return status.get().equals(com.fred4106.improvedCharges.Constants.ItemActivity.DEACTIVATED.toString());
    }

    public boolean isActivated() {
        final Optional<String> status = Optional.ofNullable(provider.configManager.getConfiguration(Constants.GROUP, getConfigStatusKey()));

        if (!status.isPresent()) {
            return false;
        }

        return status.get().equals(com.fred4106.improvedCharges.Constants.ItemActivity.ACTIVATED.toString());
    }

    public String getConfigStatusKey() {
        return configKey + "_status";
    }

    public void deactivate() {
        setActivity(com.fred4106.improvedCharges.Constants.ItemActivity.DEACTIVATED);
    }

    public void activate() {
        setActivity(com.fred4106.improvedCharges.Constants.ItemActivity.ACTIVATED);
    }

    private void setActivity(final com.fred4106.improvedCharges.Constants.ItemActivity status) {
        provider.configManager.setConfiguration(Constants.GROUP, getConfigStatusKey(), status);
    }

    @Override
    public boolean inInventoryOrEquipment() {
        return super.inInventoryOrEquipment();
    }

    @Override
    public Color getTextColor(final int itemId) {
        final Color defaultColor = super.getTextColor(itemId);

        if (defaultColor == provider.config.getColorEmpty() || isDeactivated()) {
            return provider.config.getColorEmpty();
        }

        if (isActivated()) {
            return provider.config.getColorActivated();
        }

        return defaultColor;
    }

    @Override
    public Color getTotalTextColor() {
        final Color defaultColor = super.getTotalTextColor();

        if (defaultColor == provider.config.getColorEmpty() || isDeactivated()) {
            return provider.config.getColorEmpty();
        }

        if (isActivated()) {
            return provider.config.getColorActivated();
        }

        return defaultColor;
    }
}
