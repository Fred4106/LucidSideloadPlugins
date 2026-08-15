package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.item.triggers.OnItemPickup;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.events.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ListenerOnItemPickup extends ListenerBase {
    public ListenerOnItemPickup(Provider provider) {
        super(provider);
    }

    public void trigger(ItemDespawned event, ChargedItemBase chargedItem) {
        for (TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(chargedItem, triggerBase, event)) continue;

            OnItemPickup trigger = (OnItemPickup) triggerBase;
            boolean triggerUsed = false;

            if (trigger.pickUpToStorage.isPresent()) {
                ((ChargedItemWithStorage) chargedItem).storage.add(event.getItem().getId(), event.getItem().getQuantity());
                triggerUsed = true;
            }

            if (super.trigger(trigger, chargedItem)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(ChargedItemBase chargedItemBase, TriggerBase triggerBase, ItemDespawned event) {
        if (!(triggerBase instanceof OnItemPickup)) return false;
        if (!(chargedItemBase instanceof ChargedItemWithStorage)) return false;
        OnItemPickup trigger = (OnItemPickup) triggerBase;
        ChargedItemWithStorage chargedItem = (ChargedItemWithStorage) chargedItemBase;

        // Correct item check.
        boolean correctItem = false;
        for (StorageItem storageItem : chargedItem.storage.getStorableItems()) {
            if (event.getItem().getId() == storageItem.itemId) {
                correctItem = true;
                break;
            }
        }
        if (!correctItem) {
            return false;
        }

        // By one check.
        if (trigger.isByOne.isPresent() && trigger.isByOne.get() && event.getItem().getQuantity() > 1) {
            return false;
        }

        // Menu option check.
        if (!provider.store.inMenuOptions("Take")) {
            return false;
        }

        // Menu target check.
        if (!provider.store.inMenuTargets(event.getItem().getId())) {
            return false;
        }

        // Player location check.
        if (provider.client.getLocalPlayer().getWorldLocation().distanceTo(event.getTile().getWorldLocation()) > 1) {
            return false;
        }

        return super.isValidTrigger(trigger, chargedItemBase);
    }
}
