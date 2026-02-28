package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import net.runelite.api.Skill;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.WidgetId;

import java.util.List;
import java.util.Optional;

import static com.fred4106.improvedCharges.FredsItemChargesPlugin.getNumberFromWordRepresentation;
import static com.fred4106.improvedCharges.store.ids.ItemContainerId.INVENTORY;

public class U_ReagentPouch extends ChargedItemWithStorageEmptyable {
    public U_ReagentPouch(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.REAGENT_POUCH, ItemId.REAGENT_POUCH, provider);
		storage.emptyIsNegative().setMaximumIndividualQuantity(26).storableItems(
			new StorableItem(ItemId.EYE_OF_NEWT).checkName("Eye of newt"),
			new StorableItem(ItemId.LIMPWURT_ROOT).checkName("Limpwurt root"),
			new StorableItem(ItemId.RED_SPIDERS_EGGS).checkName("Red spider's eggs", "eggs"),
			new StorableItem(ItemId.TOADS_LEGS).checkName("Toad's legs", "toad legs"),
			new StorableItem(ItemId.SNAPE_GRASS).checkName("Snape grass"),
			new StorableItem(ItemId.MORT_MYRE_FUNGUS).checkName("Mort myre fungus", "Mort Myre fungi"),
			new StorableItem(ItemId.WHITE_BERRIES).checkName("White berries"),
			new StorableItem(ItemId.JANGERBERRIES).checkName("Jangerberries"),
			new StorableItem(ItemId.POISON_IVY_BERRIES).checkName("Poison ivy berries"),
			new StorableItem(ItemId.POTATO_CACTUS).checkName("Potato cactus"),
			new StorableItem(ItemId.CACTUS_SPINE).checkName("Cactus spine"),
			new StorableItem(ItemId.ASHES).checkName("Ashes"),
			new StorableItem(ItemId.CHOCOLATE_DUST).checkName("Chocolate dust"),
			new StorableItem(ItemId.DRAGON_SCALE_DUST).checkName("Dragon scale dust", "dragon scale"),
			new StorableItem(ItemId.GOAT_HORN_DUST).checkName("Goat horn dust", "ground desert goat horn"),
			new StorableItem(ItemId.GORAK_CLAW_POWDER).checkName("Gorak claw powder"),
			new StorableItem(ItemId.KEBBIT_TEETH_DUST).checkName("Kebbit teeth dust", "ground kebbit teeth"),
			new StorableItem(ItemId.SILVER_DUST).checkName("Silver dust"),
			new StorableItem(ItemId.UNICORN_HORN_DUST).checkName("Unicorn horn dust", "unicorn horn"),
			new StorableItem(ItemId.WINE_OF_ZAMORAK).checkName("Wine of zamorak", "wine"),
			new StorableItem(ItemId.ALDARIUM).checkName("Aldarium"),
			new StorableItem(ItemId.YEW_ROOTS).checkName("Yew roots"),
			new StorableItem(ItemId.MAGIC_ROOTS).checkName("Magic roots"),
			new StorableItem(ItemId.CRUSHED_NEST).checkName("Crushed nest", "crushed bird nest"),
			new StorableItem(ItemId.CRUSHED_SUPERIOR_DRAGON_BONES).checkName("Crushed superior dragon bones"),
			new StorableItem(ItemId.NAIL_BEAST_NAILS).checkName("Nail beast nails"),
			new StorableItem(ItemId.LILY_OF_THE_SANDS).checkName("Lily of the sands"),
			new StorableItem(ItemId.CAVIAR).checkName("Caviar"),
			new StorableItem(ItemId.ROE).checkName("Roe"),
			new StorableItem(ItemId.SQUID_PASTE).checkName("Squid paste"),
			new StorableItem(ItemId.DEMONIC_TALLOW).checkName("Demonic tallow"),
			new StorableItem(ItemId.HADDOCK_EYE).checkName("Haddock eye")
		);

		this.items = new TriggerItem[]{
			new TriggerItem(ItemId.REAGENT_POUCH),
			new TriggerItem(ItemId.REAGENT_POUCH_OPEN),
		};

		this.triggers.addAll(List.of(
			// Check empty.
			new OnChatMessage("The Reagent pouch is empty.").emptyStorage(),

			// Check.
			new OnChatMessage("You look in your Reagent pouch and see:").emptyStorage(),
			new OnChatMessage("(?<amount>.+) x (?<item>.+)").matcherConsumer(m -> {
				final Optional<StorageItem> item = getStorageItemFromName(m.group("item"), Integer.parseInt(m.group("amount")));
				storage.put(item);
			}).hasChatMessage("You look in your Reagent pouch and see:"),

			// Pick up.
			new OnChatMessage("You put the (?<item>.+) into your Reagent pouch.").matcherConsumer(m -> {
				final Optional<StorageItem> item = getStorageItemFromName(m.group("item"), 1);
				storage.add(item);
			}),

			// Empty to bank.
			new OnChatMessage("You empty your pouch into the bank").onItemClick().emptyStorage(),

			// Empty to inventory.
			new OnItemContainerChanged(INVENTORY).emptyStorageToInventory().onMenuOption("Empty"),

			// Fill from inventory.
			new OnItemContainerChanged(INVENTORY).onInventoryDifference(inventoryDifference -> {
				for (final StorageItem inventoryDifferenceItem : inventoryDifference.getItems()) {
					// Item was put into the reagent pouch, but there is more in inventory, meaning that item is filled to maximum.
					if (provider.store.inventory.hasItem(inventoryDifferenceItem.getId())) {
						storage.put(inventoryDifferenceItem.getId(), 26);
					}
				}
			}).onMenuOption("Fill", FredsItemChargesPlugin.menuOptionFillFromInventory),

			new OnMenuOptionClicked("Fill", FredsItemChargesPlugin.menuOptionFillFromInventory).consumer(() -> {
				for (final StorageItem item : provider.store.inventory.getItems()) {
					storage.add(item);
				}
			}),

			// Replace "Use" with proper Fill/Empty option.
			new OnMenuEntryAdded("Use").replaceOptionConsumer(this::getMenuOptionForUse).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

			// Mix potions.
			new OnChatMessage("You mix the (?<item>.+) into (your|the unfinished)( antifire)? (potion|antidote\\+\\+).*").matcherConsumer((m) -> {
				final Optional<StorageItem> item = getStorageItemFromName(m.group("item"), 1);
				storage.remove(item);
			}).requiredItem(ItemId.REAGENT_POUCH_OPEN),

			// Harvesting
			new OnXpDrop(Skill.FARMING).onMenuOption("Harvest").onMenuTarget("Snape grass plant").addToStorage(ItemId.SNAPE_GRASS, 1),
			new OnChatMessage("You pick (?<quantity>.+) mushrooms? from the log.").matcherConsumer(m -> {
				storage.add(ItemId.MORT_MYRE_FUNGUS, getNumberFromWordRepresentation(m.group("quantity")));
			}),
			new OnChatMessage("You pick some whiteberries.").requiredItem(ItemId.REAGENT_POUCH_OPEN).consumer(() -> {
				storage.add(ItemId.WHITE_BERRIES, 1);
			}),
			new OnChatMessage("You pick some jangerberries.").requiredItem(ItemId.REAGENT_POUCH_OPEN).consumer(() -> {
				storage.add(ItemId.JANGERBERRIES, 1);
			}),
			new OnChatMessage("You pick some poison ivy berries.").requiredItem(ItemId.REAGENT_POUCH_OPEN).consumer(() -> {
				storage.add(ItemId.POISON_IVY_BERRIES, 1);
			}),
			new OnChatMessage("You carefully pick a potato cactus.").requiredItem(ItemId.REAGENT_POUCH_OPEN).consumer(() -> {
				storage.add(ItemId.POTATO_CACTUS, 1);
			}),
			new OnChatMessage("You carefully pick a spine from the cactus.").consumer(() -> {
				storage.add(ItemId.CACTUS_SPINE, 1);
			}),


			// Hide destroy.
			new OnMenuEntryAdded("Destroy").hide()
		));
	}

	private String getMenuOptionForUse() {
		return storage.isStorableItemInInventory()
			? FredsItemChargesPlugin.menuOptionFillFromInventory
			: FredsItemChargesPlugin.menuOptionEmptyToBank;
	}
}
