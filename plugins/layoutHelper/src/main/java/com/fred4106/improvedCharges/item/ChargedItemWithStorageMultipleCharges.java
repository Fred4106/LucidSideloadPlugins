package com.fred4106.improvedCharges.item;

import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.store.*;

public class ChargedItemWithStorageMultipleCharges extends ChargedItemWithStorage {
    public ChargedItemWithStorageMultipleCharges(String configKey, int itemId, Provider provider) {
        super(configKey, itemId, provider);
    }

    @Override
    public String getChargesString(int itemId) {
        return getTotalChargesString();
    }

    @Override
    public String getTotalChargesString() {
        String individualCharges = "";

        int validItems = 0;
        for (StorageItem storageItem : getStorage().getItems()) {
            if (storageItem.getQuantity() >= 0) {
                individualCharges += storageItem.getQuantity() + "/";
                validItems++;
            }
        }

        return validItems > 0 ? individualCharges.replaceAll("/$", "") : "?";
    }
}
