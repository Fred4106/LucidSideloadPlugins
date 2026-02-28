package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import com.fred4106.improvedCharges.store.ids.ItemId;
import net.runelite.api.Skill;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.WidgetId;

import java.util.List;

import static com.fred4106.improvedCharges.store.ids.ItemContainerId.INVENTORY;
import static com.fred4106.improvedCharges.store.ids.ItemContainerId.BANK;

public class U_GemBag extends ChargedItemWithStorageEmptyable {
    public U_GemBag(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.GEM_BAG, ItemId.GEM_BAG, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.GEM_BAG),
            new TriggerItem(ItemId.GEM_BAG_OPEN),
        };

        storage.setMaximumIndividualQuantity(60).storableItems(
            new StorableItem(ItemId.UNCUT_SAPPHIRE).checkName("Sapphire"),
            new StorableItem(ItemId.UNCUT_EMERALD).checkName("Emerald"),
            new StorableItem(ItemId.UNCUT_RUBY).checkName("Ruby"),
            new StorableItem(ItemId.UNCUT_DIAMOND).checkName("Diamond"),
            new StorableItem(ItemId.UNCUT_DRAGONSTONE).checkName("Dragonstone")
        );

        this.triggers.addAll(List.of(
            // Empty to bank or inventory.
            new OnChatMessage("The gem bag is( now)? empty.").emptyStorage(),

            // Empty and Check.
            new OnChatMessage("(Left in bag: )?Sapphires: (?<sapphires>.+) / Emeralds: (?<emeralds>.+) / Rubies: (?<rubies>.+) Diamonds: (?<diamonds>.+) / Dragonstones: (?<dragonstones>.+)").matcherConsumer(m -> {
                storage.put(ItemId.UNCUT_SAPPHIRE, Integer.parseInt(m.group("sapphires")));
                storage.put(ItemId.UNCUT_EMERALD, Integer.parseInt(m.group("emeralds")));
                storage.put(ItemId.UNCUT_RUBY, Integer.parseInt(m.group("rubies")));
                storage.put(ItemId.UNCUT_DIAMOND, Integer.parseInt(m.group("diamonds")));
                storage.put(ItemId.UNCUT_DRAGONSTONE, Integer.parseInt(m.group("dragonstones")));
            }),

            // Mining regular or gem rocks.
            new OnChatMessage("You just (found|mined) (a|an) (?<gem>.+)!").matcherConsumer(m -> {
                storage.add(getStorageItemFromName(m.group("gem"), 1));
            }).requiredItem(ItemId.GEM_BAG_OPEN),

            // Pickpocketing.
            new OnChatMessage("The following stolen loot gets added to your gem bag: Uncut (?<gem>.+) x (?<quantity>.+).").matcherConsumer(m -> {
                storage.add(getStorageItemFromName(m.group("gem"), Integer.parseInt(m.group("quantity"))));
            }),

            // Stealing from stalls.
            new OnChatMessage("You steal an uncut (?<gem>.+) and add it to your gem bag.").matcherConsumer(m -> {
                storage.add(getStorageItemFromName(m.group("gem"), 1));
            }),

            // Fill from inventory.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onMenuOption("Fill"),

            // Empty to bank.
            new OnItemContainerChanged(BANK).emptyStorageToBank().onMenuOption(FredsItemChargesPlugin.menuOptionEmptyToBank),

            // Use gem on bag
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onUseChargedItemOnStorageItem(storage.getStorableItems()),
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onUseStorageItemOnChargedItem(storage.getStorableItems()),

            // Pick up.
            new OnItemPickup(storage.getStorableItems()).isByOne().requiredItem(ItemId.GEM_BAG_OPEN).pickUpToStorage(),

            // Telegrab.
            new OnXpDrop(Skill.MAGIC).requiredItem(ItemId.GEM_BAG_OPEN).onMenuOption("Cast").onMenuTarget(
                "Uncut sapphire"
            ).addToStorage(ItemId.UNCUT_SAPPHIRE, 1),
            new OnXpDrop(Skill.MAGIC).requiredItem(ItemId.GEM_BAG_OPEN).onMenuOption("Cast").onMenuTarget(
                "Uncut emerald"
            ).addToStorage(ItemId.UNCUT_EMERALD, 1),
            new OnXpDrop(Skill.MAGIC).requiredItem(ItemId.GEM_BAG_OPEN).onMenuOption("Cast").onMenuTarget(
                "Uncut ruby"
            ).addToStorage(ItemId.UNCUT_RUBY, 1),
            new OnXpDrop(Skill.MAGIC).requiredItem(ItemId.GEM_BAG_OPEN).onMenuOption("Cast").onMenuTarget(
                "Uncut diamond"
            ).addToStorage(ItemId.UNCUT_DIAMOND, 1),
            new OnXpDrop(Skill.MAGIC).requiredItem(ItemId.GEM_BAG_OPEN).onMenuOption("Cast").onMenuTarget(
                "Uncut dragonstone"
            ).addToStorage(ItemId.UNCUT_DRAGONSTONE, 1),

            // Replace "Empty" with proper "Empty to bank".
            new OnMenuEntryAdded("Empty").replaceOption(FredsItemChargesPlugin.menuOptionEmptyToBank).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Hide destroy.
            new OnMenuEntryAdded("Destroy").hide()
        ));
    }
}
