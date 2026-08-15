package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.events.CustomItemContainerChanged;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.item.storage.StorageItems;
import com.fred4106.improvedCharges.item.triggers.OnItemContainerChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.events.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

public class ListenerOnItemContainerChanged extends ListenerBase {
    public ListenerOnItemContainerChanged(Provider provider) {
        super(provider);
    }

    public void trigger(CustomItemContainerChanged itemContainerChanged, ChargedItemBase chargedItem) {
        // Get quantity from amount in item container.
        for (TriggerItem triggerItem : chargedItem.items) {
            if (triggerItem.quantityCharges.isPresent()) {
               for (StorageItem item : itemContainerChanged.getItems()) {
                    if (item.itemId == triggerItem.itemId) {
                        ((ChargedItem) chargedItem).setCharges(item.getQuantity());
                        break;
                    }
                }
            }
        }

        for (TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(chargedItem, triggerBase, itemContainerChanged)) continue;
            boolean triggerUsed = false;
            OnItemContainerChanged trigger = (OnItemContainerChanged) triggerBase;

            // Update storage directly from item container.
            if (trigger.updateStorage.isPresent()) {
                ((ChargedItemWithStorage) chargedItem).storage.updateFromItemContainer(itemContainerChanged);
                triggerUsed = true;
            }

            if (trigger.onInventoryDifference.isPresent()) {
                trigger.onInventoryDifference.get().accept(provider.store.getInventoryItemsDifference());
                triggerUsed = true;
            }

            if (trigger.onBankDifference.isPresent()) {
                trigger.onBankDifference.get().accept(provider.store.getBankItemsDifference());
                triggerUsed = true;
            }

            if (trigger.itemsConsumer.isPresent()) {
                trigger.itemsConsumer.get().accept(new StorageItems(itemContainerChanged));
                triggerUsed = true;
            }

            if (super.trigger(trigger, chargedItem)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(ChargedItemBase chargedItem, TriggerBase triggerBase, CustomItemContainerChanged itemContainerChanged) {
        if (!(triggerBase instanceof OnItemContainerChanged)) return false;
        OnItemContainerChanged trigger = (OnItemContainerChanged) triggerBase;

        // Item container type check.
        if (
            itemContainerChanged.getContainerId() != trigger.itemContainerId) {
            return false;
        }

        return super.isValidTrigger(trigger, chargedItem);
    }
}
