package com.fred4106.improvedCharges.item.listeners;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.ChargedItemBase;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.events.CustomItemContainerChanged;
import com.fred4106.improvedCharges.item.storage.StorageItems;
import com.fred4106.improvedCharges.item.triggers.OnItemContainerChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

public class ListenerOnItemContainerChanged extends ListenerBase {
    public ListenerOnItemContainerChanged(final Provider provider, final ChargedItemBase chargedItem) {
        super(provider, chargedItem);
    }

    public void trigger(final CustomItemContainerChanged itemContainerChanged) {
        // Get quantity from amount in item container.
        for (final TriggerItem triggerItem : chargedItem.items) {
            if (triggerItem.quantityCharges.isPresent()) {
               for (final StorageItem item : itemContainerChanged.getItems()) {
                    if (item.getId() == triggerItem.itemId) {
                        ((ChargedItem) chargedItem).setCharges(item.getQuantity());
                        break;
                    }
                }
            }
        }

        for (final TriggerBase triggerBase : chargedItem.triggers) {
            if (!isValidTrigger(triggerBase, itemContainerChanged)) continue;
            boolean triggerUsed = false;
            final OnItemContainerChanged trigger = (OnItemContainerChanged) triggerBase;

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

            if (super.trigger(trigger)) {
                triggerUsed = true;
            }

            if (triggerUsed) return;
        }
    }

    public boolean isValidTrigger(final TriggerBase triggerBase, final CustomItemContainerChanged itemContainerChanged) {
        if (!(triggerBase instanceof OnItemContainerChanged)) return false;
        final OnItemContainerChanged trigger = (OnItemContainerChanged) triggerBase;

        // Item container type check.
        if (
            itemContainerChanged.getContainerId() != trigger.itemContainerId) {
            return false;
        }

        return super.isValidTrigger(trigger);
    }
}
