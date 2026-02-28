package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.WidgetId;

import java.util.List;

import static com.fred4106.improvedCharges.store.ids.ItemContainerId.INVENTORY;

public class U_FlamtaerBag extends ChargedItemWithStorageEmptyable {
//    private boolean flamtaerBagEmptyDialogVisible = false;

    public U_FlamtaerBag(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.FLAMTAER_BAG, ItemId.FLAMTAER_BAG, provider);
        storage.storableItems(
            new StorableItem(ItemId.TIMBER_BEAM),
            new StorableItem(ItemId.LIMESTONE_BRICK),
            new StorableItem(ItemId.SWAMP_PASTE)
        );

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.FLAMTAER_BAG),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Timber beams: (?<beams>.+) Limestone bricks: (?<bricks>.+) Swamp paste: (?<paste>.+)").matcherConsumer(m -> {
                storage.clear();
                storage.put(ItemId.TIMBER_BEAM, Integer.parseInt(m.group("beams")));
                storage.put(ItemId.LIMESTONE_BRICK, Integer.parseInt(m.group("bricks")));
                storage.put(ItemId.SWAMP_PASTE, Integer.parseInt(m.group("paste")));
            }),

            // Repaired.
            new OnChatMessage("Your temple repair resource pool is full").consumer(() -> {
                storage.removeAndPrioritizeInventory(ItemId.TIMBER_BEAM, 1);
                storage.removeAndPrioritizeInventory(ItemId.LIMESTONE_BRICK, 1);
                storage.removeAndPrioritizeInventory(ItemId.SWAMP_PASTE, 5);
            }),

            // Replace "Empty" with proper "Empty to inventory" at bank.
            new OnMenuEntryAdded("Empty").replaceOption(FredsItemChargesPlugin.menuOptionEmptyToInventory).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Fill from inventory.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onMenuOption("Fill"),

            // Empty to inventory at bank.
            new OnItemContainerChanged(INVENTORY).emptyStorageToInventory().onMenuOption(FredsItemChargesPlugin.menuOptionEmptyToInventory),

            // Use storable items on flamtaer bag.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onUseStorageItemOnChargedItem(storage.getStorableItems()),

            // Use flamtaer bag on storable item.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onUseChargedItemOnStorageItem(storage.getStorableItems()),

//            // Flamtaer empty widget appeared.
//            new OnWidgetLoaded(219, 1).widgetConsumer(widget -> {
//                final Optional<Widget> emptyEverything = Optional.ofNullable(widget.getChild(1));
//                final Optional<Widget> emptyFirstOption = Optional.ofNullable(widget.getChild(2));
//
//                flamtaerBagEmptyDialogVisible = (
//                    emptyEverything.isPresent() && emptyEverything.get().getText().equals("Everything") &&
//                    emptyFirstOption.isPresent() && (
//                        emptyFirstOption.get().getText().contains("Timber beams") ||
//                        emptyFirstOption.get().getText().contains("Limestone bricks") ||
//                        emptyFirstOption.get().getText().contains("Swamp paste")
//                    )
//                );
//            }),
//            // TODO - figure out how to detect which option was chosen from empty dialog

            // Trying to empty already empty bag.
            new OnChatMessage("The bag is empty").onMenuOption("Empty", FredsItemChargesPlugin.menuOptionEmptyToInventory).onItemClick().emptyStorage(),

            // Hide destroy.
            new OnMenuEntryAdded("Destroy").hide()
        ));
    }
}
