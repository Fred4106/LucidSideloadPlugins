package com.fred4106.improvedCharges.item;

import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ChargedItemWithStorageEmptyable extends ChargedItemWithStorage {
    public ChargedItemWithStorageEmptyable(String configKey, int itemId, Provider provider) {
        super(configKey, itemId, provider);

        this.triggers.add(
            new OnChatMessage("You empty all of your containers into the bank.").emptyStorage()
        );
    }
}
