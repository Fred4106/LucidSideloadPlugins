package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.Skill;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.ids.ItemContainerId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.ids.WidgetId;

import java.util.List;

import static com.fred4106.improvedCharges.store.ids.ItemContainerId.BANK;
import static com.fred4106.improvedCharges.store.ids.ItemContainerId.INVENTORY;

public class U_FurPouch extends ChargedItemWithStorageEmptyable {
    public U_FurPouch(final Provider provider) {
        super(Constants.FUR_POUCH, ItemId.FUR_POUCH_SMALL, provider);
        this.storage = storage.storableItems(
            // Tracking.
            new StorableItem(ItemId.POLAR_KEBBIT_FUR),
            new StorableItem(ItemId.COMMON_KEBBIT_FUR),
            new StorableItem(ItemId.FELDIP_WEASEL_FUR),
            new StorableItem(ItemId.DESERT_DEVIL_FUR),

            // Deadfall.
            new StorableItem(ItemId.FOX_FUR),

            // Pitfalls.
            new StorableItem(ItemId.LARUPIA_FUR),
            new StorableItem(ItemId.GRAAHK_FUR),
            new StorableItem(ItemId.KYATT_FUR),
            new StorableItem(ItemId.SUNLIGHT_ANTELOPE_FUR),
            new StorableItem(ItemId.MOONLIGHT_ANTELOPE_FUR),

            // Aerial.
            new StorableItem(ItemId.SPOTTED_KEBBIT_FUR),
            new StorableItem(ItemId.DARK_KEBBIT_FUR),
            new StorableItem(ItemId.DASHING_KEBBIT_FUR)
        );

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.FUR_POUCH_SMALL).maxCharges(14),
            new TriggerItem(ItemId.FUR_POUCH_SMALL_OPEN).maxCharges(14),
            new TriggerItem(ItemId.FUR_POUCH_MEDIUM).maxCharges(21),
            new TriggerItem(ItemId.FUR_POUCH_MEDIUM_OPEN).maxCharges(21),
            new TriggerItem(ItemId.FUR_POUCH_LARGE).maxCharges(28),
            new TriggerItem(ItemId.FUR_POUCH_LARGE_OPEN).maxCharges(28),
        };

        this.triggers.addAll(List.of(
            // Empty.
            new OnChatMessage("Your fur pouch is currently holding 0 fur.").emptyStorage(),
            new OnChatMessage("Your fur pouch is empty.").emptyStorage(),

            // Fill from inventory.
            new OnItemContainerChanged(ItemContainerId.INVENTORY).fillStorageFromInventory().onMenuOption("Fill"),

            // Empty to inventory.
            new OnItemContainerChanged(ItemContainerId.INVENTORY).emptyStorageToInventory().onMenuOption("Empty"),

            // Empty to bank.
            new OnItemContainerChanged(BANK).emptyStorageToBank().onMenuOption(FredsItemChargesPlugin.menuOptionEmptyToBank),

            // Empty from deposit box.
            new OnMenuOptionClicked(FredsItemChargesPlugin.menuOptionEmptyToBank).onItemClick().isWidgetVisible(WidgetId.DEPOSIT_BOX).emptyStorage(),

            // Use fur on pouch.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onUseStorageItemOnChargedItem(storage.getStorableItems()),

            // Replace "Empty" with proper "Empty to bank".
            new OnMenuEntryAdded("Empty").replaceOption(FredsItemChargesPlugin.menuOptionEmptyToBank).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Hide destroy option.
            new OnMenuEntryAdded("Destroy").hide(),

            // Tracking.
            new OnChatMessage("You manage to noose a polar kebbit that is hiding in the snowdrift.").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.POLAR_KEBBIT_FUR),
            new OnChatMessage("You manage to noose a common kebbit that is hiding in the bush.").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.COMMON_KEBBIT_FUR),
            new OnChatMessage("You manage to noose a Feldip weasel that is hiding in the bush.").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.FELDIP_WEASEL_FUR),
            new OnChatMessage("You manage to noose a desert devil that is hiding in the sand.").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.DESERT_DEVIL_FUR),

            // Deadfalls.
            new OnChatMessage("You've caught a pyre fox.").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.FOX_FUR),

            // Pitfalls.
            new OnChatMessage("You've caught a spined larupia!").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.LARUPIA_FUR),
            new OnItemContainerChanged(ItemContainerId.INVENTORY).hasChatMessage("You've caught a spined larupia!").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).onInventoryDifference(itemsDifference -> {
                if (itemsDifference.hasItem(ItemId.LARUPIA_FUR_TATTY)) {
                    storage.remove(ItemId.LARUPIA_FUR, 1);
                }
            }),
            new OnChatMessage("You've caught a horned graahk!").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.GRAAHK_FUR),
            new OnItemContainerChanged(ItemContainerId.INVENTORY).hasChatMessage("You've caught a horned graahk!").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).onInventoryDifference(itemsDifference -> {
                if (itemsDifference.hasItem(ItemId.GRAAHK_FUR_TATTY)) {
                    storage.remove(ItemId.GRAAHK_FUR, 1);
                }
            }),
            new OnChatMessage("You've caught a sabre-?toothed kyatt!").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.KYATT_FUR),
            new OnItemContainerChanged(ItemContainerId.INVENTORY).hasChatMessage("You've caught a sabre-?toothed kyatt!").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).onInventoryDifference(itemsDifference -> {
                if (itemsDifference.hasItem(ItemId.KYATT_FUR_TATTY)) {
                    storage.remove(ItemId.KYATT_FUR, 1);
                }
            }),
            new OnChatMessage("You've caught a sunlight antelope!").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.SUNLIGHT_ANTELOPE_FUR),
            new OnChatMessage("You've caught a moonlight antelope!").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).addToStorage(ItemId.MOONLIGHT_ANTELOPE_FUR),

            // Aerial.
            new OnXpDrop(Skill.HUNTER, 104).hasChatMessage("You retrieve the falcon as well as the fur of the dead kebbit.").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).consumer(() -> {
                storage.add(ItemId.SPOTTED_KEBBIT_FUR, 1);
            }),
            new OnXpDrop(Skill.HUNTER, 132).hasChatMessage("You retrieve the falcon as well as the fur of the dead kebbit.").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).consumer(() -> {
                storage.add(ItemId.DARK_KEBBIT_FUR, 1);
            }),
            new OnXpDrop(Skill.HUNTER, 156).hasChatMessage("You retrieve the falcon as well as the fur of the dead kebbit.").requiredItem(ItemId.FUR_POUCH_SMALL_OPEN, ItemId.FUR_POUCH_MEDIUM_OPEN, ItemId.FUR_POUCH_LARGE_OPEN).consumer(() -> {
                storage.add(ItemId.DASHING_KEBBIT_FUR, 1);
            })
        ));
    }
}
