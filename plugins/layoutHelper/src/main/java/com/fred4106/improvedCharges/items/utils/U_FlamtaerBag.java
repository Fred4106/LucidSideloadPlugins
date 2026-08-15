package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnItemContainerChanged;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.WidgetId;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.*;

import java.util.List;

public class U_FlamtaerBag extends ChargedItemWithStorageEmptyable {
//    private boolean flamtaerBagEmptyDialogVisible = false;

    public U_FlamtaerBag(Provider provider) {
        super(FredsItemChargesConfig.flamtaer_bag, ItemID.FLAMTAER_BAG, provider);
        storage.storableItems(
            new StorableItem(ItemID.TIMBERBEAM),
            new StorableItem(ItemID.LIMESTONEBRICK),
            new StorableItem(ItemID.SWAMPPASTE)
        );

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.FLAMTAER_BAG),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Timber beams: (?<beams>.+) Limestone bricks: (?<bricks>.+) Swamp paste: (?<paste>.+)").matcherConsumer(m -> {
                storage.clear();
                storage.put(ItemID.TIMBERBEAM, Integer.parseInt(m.group("beams")));
                storage.put(ItemID.LIMESTONEBRICK, Integer.parseInt(m.group("bricks")));
                storage.put(ItemID.SWAMPPASTE, Integer.parseInt(m.group("paste")));
            }),

            // Repaired.
            new OnChatMessage("Your temple repair resource pool is full").consumer(() -> {
                storage.removeAndPrioritizeInventory(ItemID.TIMBERBEAM, 1);
                storage.removeAndPrioritizeInventory(ItemID.LIMESTONEBRICK, 1);
                storage.removeAndPrioritizeInventory(ItemID.SWAMPPASTE, 5);
            }),

            // Replace "Empty" with proper "Empty to inventory" at bank.
            new OnMenuEntryAdded("Empty").replaceOption(FredsItemChargesPlugin.menuOptionEmptyToInventory).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Fill from inventory.
            new OnItemContainerChanged(InventoryID.INV).fillStorageFromInventory().onMenuOption("Fill"),

            // Empty to inventory at bank.
            new OnItemContainerChanged(InventoryID.INV).emptyStorageToInventory().onMenuOption(FredsItemChargesPlugin.menuOptionEmptyToInventory),

            // Use storable items on flamtaer bag.
            new OnItemContainerChanged(InventoryID.INV).fillStorageFromInventory().onUseStorageItemOnChargedItem(storage.getStorableItems()),

            // Use flamtaer bag on storable item.
            new OnItemContainerChanged(InventoryID.INV).fillStorageFromInventory().onUseChargedItemOnStorageItem(storage.getStorableItems()),

//            // Flamtaer empty widget appeared.
//            new OnWidgetLoaded(219, 1).widgetConsumer(widget -> {
//                Optional<Widget> emptyEverything = Optional.ofNullable(widget.getChild(1));
//                Optional<Widget> emptyFirstOption = Optional.ofNullable(widget.getChild(2));
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
