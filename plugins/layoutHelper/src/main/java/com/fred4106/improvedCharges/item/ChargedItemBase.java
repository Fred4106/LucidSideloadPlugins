package com.fred4106.improvedCharges.item;

import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import net.runelite.client.ui.*;
import net.runelite.client.util.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.ids.*;

import javax.annotation.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class ChargedItemBase {
    public Provider provider;

    String configKey;
    public int itemId;

    public TriggerItem[] items = new TriggerItem[]{};
    public List<TriggerBase> triggers = new ArrayList<>();



    public boolean inInventory = false;
    public boolean inEquipment = false;

    public ChargedItemBase(
        String configKey,
        int itemId,
        Provider provider
    ) {
        this.provider = provider;

        this.itemId = itemId;
        this.configKey = configKey;
    }

    public abstract int getCharges(int itemId);

    public abstract int getTotalCharges();

    public String getChargesString(int itemId) {
        return FredsItemChargesPlugin.getChargesMinified(getCharges(itemId));
    }

    public String getLongChargesString(int itemId) {
        int charges = getCharges(itemId);

        // Unlimited.
        if (charges == ChargeId.UNLIMITED) return FredsItemChargesPlugin.INFINITE_SYMBOL;

        // Unknown.
        if (charges == ChargeId.UNKNOWN) return "?";

        return String.valueOf(charges);
    }

    public String getTotalChargesString() {
        return FredsItemChargesPlugin.getChargesMinified(getTotalCharges());
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getInfoboxName() {
        return configKey;
    }

    public boolean inInventory() {
        return inInventory;
    }

    public boolean inEquipment() {
        return inEquipment;
    }

    public boolean inInventoryOrEquipment() {
        return inInventory() || inEquipment();
    }

    public String getTooltip() {
        return getItemName() + (needsToBeEquipped() && !inEquipment() ? " (needs to be equipped)" : "") + ": " + ColorUtil.wrapWithColorTag(getLongChargesString(itemId), JagexColors.MENU_TARGET);
    }

    @Nonnull
    private TriggerItem getCurrentItem() {
        for (TriggerItem triggerItem : items) {
            if (triggerItem.itemId == itemId) {
                return triggerItem;
            }
        }

        return null;
    }

    public String getItemName() {
        return provider.itemManager.getItemComposition(itemId).getName();
    }

    public boolean needsToBeEquipped() {
        return getCurrentItem().needsToBeEquipped.isPresent();
    }

    public Optional<Integer> getMaxCharges() {
        for (TriggerItem item : items) {
            if (item.itemId == itemId && item.maxCharges.isPresent()) {
                return item.maxCharges;
            }
        }

        return Optional.empty();
    }

    private Color getColorForCharges(int charges) {
        if (charges == ChargeId.UNKNOWN) {
            return provider.config.getColorUnknown();
        }

        if (charges == 0) {
            return provider.config.getColorEmpty();
        }

        if (needsToBeEquipped() && inEquipment()) {
            return provider.config.getColorActivated();
        }

        if (needsToBeEquipped() && !inEquipment()) {
            return provider.config.getColorEmpty();
        }

        return provider.config.getColorDefault();
    }

    public Color getTotalTextColor() {
        return getColorForCharges(getTotalCharges());
    }

    public Color getTextColor(int itemId) {
        return getColorForCharges(getCharges(itemId));
    }
}
