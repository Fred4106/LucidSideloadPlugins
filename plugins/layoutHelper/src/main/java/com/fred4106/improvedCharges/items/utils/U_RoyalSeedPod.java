package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class U_RoyalSeedPod extends ChargedItem {
    public U_RoyalSeedPod(Provider provider) {
        super(com.fred4106.improvedCharges.Constants.ROYAL_SEED_POD, ItemId.ROYAL_SEED_POD, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.ROYAL_SEED_POD).fixedCharges(ChargeId.UNLIMITED),
        };

        this.triggers = new TriggerBase[]{
            // Unify teleport.
            new OnMenuEntryAdded("Commune").replaceOption("Teleport"),

            // Hide destroy.
            new OnMenuEntryAdded("Destroy").hide(),
        };
    }
}
