package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.ids.ItemId;
import net.runelite.api.Skill;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.WidgetId;

import static com.fred4106.improvedCharges.store.ids.ItemContainerId.BANK;
import static com.fred4106.improvedCharges.store.ids.ItemContainerId.INVENTORY;

public class U_MeatPouch extends ChargedItemWithStorage {
    public U_MeatPouch(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.MEAT_POUCH, ItemId.MEAT_POUCH_SMALL, provider);
        this.storage = storage.storableItems(
            // Tracking.
            new StorableItem(ItemId.RAW_BEAST_MEAT),

            // Deadfall.
            new StorableItem(ItemId.RAW_WILD_KEBBIT),
            new StorableItem(ItemId.RAW_BARBTAILED_KEBBIT),
            new StorableItem(ItemId.RAW_PYRE_FOX),

            // Pitfalls.
            new StorableItem(ItemId.RAW_LARUPIA),
            new StorableItem(ItemId.RAW_GRAAHK),
            new StorableItem(ItemId.RAW_KYATT),
            new StorableItem(ItemId.RAW_SUNLIGHT_ANTELOPE),
            new StorableItem(ItemId.RAW_MOONLIGHT_ANTELOPE),

            // Aerial.
            new StorableItem(ItemId.RAW_DASHING_KEBBIT)
        );

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.MEAT_POUCH_SMALL).maxCharges(14),
            new TriggerItem(ItemId.MEAT_POUCH_SMALL_OPEN).maxCharges(14),
            new TriggerItem(ItemId.MEAT_POUCH_LARGE).maxCharges(28),
            new TriggerItem(ItemId.MEAT_POUCH_LARGE_OPEN).maxCharges(28),
        };

        this.triggers = new TriggerBase[]{
            // Empty.
            new OnChatMessage("Your meat pouch is currently holding 0 meat").emptyStorage(),
            new OnChatMessage("Your meat pouch is empty.").emptyStorage(),

            // Fill from inventory.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onMenuOption("Fill"),

            // Empty to inventory.
            new OnItemContainerChanged(INVENTORY).emptyStorageToInventory().onMenuOption("Empty"),

            // Empty to bank.
            new OnItemContainerChanged(BANK).emptyStorageToBank().onMenuOption(FredsItemChargesPlugin.menuOptionEmptyToBank),

            // Empty from deposit box.
            new OnMenuOptionClicked(FredsItemChargesPlugin.menuOptionEmptyToBank).onItemClick().isWidgetVisible(WidgetId.DEPOSIT_BOX).emptyStorage(),

            // Use meat on pouch.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onUseStorageItemOnChargedItem(storage.getStorableItems()),

            // Replace "Empty" with proper "Empty to bank".
            new OnMenuEntryAdded("Empty").replaceOption(FredsItemChargesPlugin.menuOptionEmptyToBank).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Hide destroy option.
            new OnMenuEntryAdded("Destroy").hide(),

            // Tracking.
            new OnChatMessage("You manage to noose a polar kebbit that is hiding in the snowdrift.").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_BEAST_MEAT),
            new OnChatMessage("You manage to noose a common kebbit that is hiding in the bush.").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_BEAST_MEAT),
            new OnChatMessage("You manage to noose a Feldip weasel that is hiding in the bush.").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_BEAST_MEAT),
            new OnChatMessage("You manage to noose a desert devil that is hiding in the sand.").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_BEAST_MEAT),
            new OnChatMessage("You manage to noose a razor-backed kebbit that is hiding in the bush.").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_BEAST_MEAT),

            // Deadfall.
            new OnChatMessage("You've caught a wild kebbit.").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_WILD_KEBBIT),
            new OnChatMessage("You've caught a barb-tailed kebbit.").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_BARBTAILED_KEBBIT),
            new OnChatMessage("You've caught a pyre fox.").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_PYRE_FOX),

            // Pitfalls.
            new OnChatMessage("You've caught a spined larupia!").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_LARUPIA),
            new OnChatMessage("You've caught a horned graahk!").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_GRAAHK),
            new OnChatMessage("You've caught a sabre-?toothed kyatt!").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_KYATT),
            new OnChatMessage("You've caught a sunlight antelope!").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_SUNLIGHT_ANTELOPE),
            new OnChatMessage("You've caught a moonlight antelope!").requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).addToStorage(ItemId.RAW_MOONLIGHT_ANTELOPE),

            // Aerial.
            new OnXpDrop(Skill.HUNTER, 156).requiredItem(ItemId.MEAT_POUCH_SMALL_OPEN, ItemId.MEAT_POUCH_LARGE_OPEN).hasChatMessage("You retrieve the falcon as well as the fur of the dead kebbit.").consumer(() -> {
                storage.add(ItemId.RAW_DASHING_KEBBIT, 1);
            }),
        };
    }
}
