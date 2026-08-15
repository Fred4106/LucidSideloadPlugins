package com.fred4106.improvedCharges.item;

import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.store.*;

import java.awt.*;
import java.util.*;

public class ChargedItemWithStatus extends ChargedItem {
    public ChargedItemWithStatus(String configKey, int itemId, Provider provider) {
        super(configKey, itemId, provider);
    }

    public boolean isDeactivated() {
        Optional<String> status = Optional.ofNullable(provider.configManager.getConfiguration(FredsItemChargesConfig.group, getConfigStatusKey()));

        if (!status.isPresent()) {
            return false;
        }

        return status.get().equals(FredsItemChargesConfig.ItemActivity.DEACTIVATED.toString());
    }

    public boolean isActivated() {
        Optional<String> status = Optional.ofNullable(provider.configManager.getConfiguration(FredsItemChargesConfig.group, getConfigStatusKey()));

        if (!status.isPresent()) {
            return false;
        }

        return status.get().equals(FredsItemChargesConfig.ItemActivity.ACTIVATED.toString());
    }

    public String getConfigStatusKey() {
        return configKey + "_status";
    }

    public void deactivate() {
        setActivity(FredsItemChargesConfig.ItemActivity.DEACTIVATED);
    }

    public void activate() {
        setActivity(FredsItemChargesConfig.ItemActivity.ACTIVATED);
    }

    private void setActivity(FredsItemChargesConfig.ItemActivity status) {
        provider.configManager.setConfiguration(FredsItemChargesConfig.group, getConfigStatusKey(), status);
    }

    @Override
    public boolean inInventoryOrEquipment() {
        return super.inInventoryOrEquipment();
    }

    @Override
    public Color getTextColor(int itemId) {
        Color defaultColor = super.getTextColor(itemId);

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
        Color defaultColor = super.getTotalTextColor();

        if (defaultColor == provider.config.getColorEmpty() || isDeactivated()) {
            return provider.config.getColorEmpty();
        }

        if (isActivated()) {
            return provider.config.getColorActivated();
        }

        return defaultColor;
    }
}
