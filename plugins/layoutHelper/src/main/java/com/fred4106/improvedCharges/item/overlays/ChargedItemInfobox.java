package com.fred4106.improvedCharges.item.overlays;

import com.fred4106.improvedCharges.Constants;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.store.Provider;

import java.awt.Color;
import java.util.Optional;

import static com.fred4106.improvedCharges.FredsItemChargesPlugin.INFINITE_SYMBOL;

public class ChargedItemInfobox extends InfoBox {
    private final Provider provider;
    private final ChargedItemBase chargedItem;

    private int itemId;

    public ChargedItemInfobox(
        final Provider provider,
        final ChargedItemBase chargedItem
    ) {
        super(provider.itemManager.getImage(chargedItem.itemId), provider.plugin);
        this.provider = provider;
        this.chargedItem = chargedItem;
        this.itemId = chargedItem.itemId;
    }

    @Override
    public String getName() {
        return super.getName() + "_" + chargedItem.getInfoboxName();
    }

    @Override
    public String getText() {
        return chargedItem.getTotalChargesString();
    }

    @Override
    public String getTooltip() {
        return chargedItem.getTooltip();
    }

    @Override
    public Color getTextColor() {
        return chargedItem.getTotalTextColor();
    }

    @Override
    public boolean render() {
        updateInfobox();

        if (
            !provider.config.showInfoboxes() ||
            !isChargedItemInfoboxEnabled() ||
            !chargedItem.inInventoryOrEquipment() ||
            chargedItem.getChargesString(itemId).equals(INFINITE_SYMBOL) && !provider.config.showUnlimited()
        ) {
            return false;
        }

        return true;
    }

    private void updateInfobox() {
        if (itemId != chargedItem.itemId) {
            // Update infobox id to keep track of correct item.
            itemId = chargedItem.itemId;

            // Update infobox image.
            setImage(provider.itemManager.getImage(itemId));
            provider.infoBoxManager.updateInfoBoxImage(this);
        }
    }

    private boolean isChargedItemInfoboxEnabled() {
        final String configKey = chargedItem.getConfigKey() + Constants._INFOBOX;
        final Optional<String> visible = Optional.ofNullable(provider.configManager.getConfiguration(Constants.GROUP, configKey));
        return visible.isPresent() && visible.get().equals("true");
    }
}


