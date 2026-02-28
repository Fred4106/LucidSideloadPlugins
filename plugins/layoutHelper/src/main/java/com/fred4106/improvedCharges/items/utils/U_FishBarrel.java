package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnItemContainerChanged;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.WidgetId;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fred4106.improvedCharges.store.ids.ItemContainerId.INVENTORY;

public class U_FishBarrel extends ChargedItemWithStorageEmptyable {
    private Optional<StorageItem> lastCaughtFish = Optional.empty();

    public U_FishBarrel(final Provider provider) {
        super(Constants.FISH_BARREL, ItemId.FISH_BARREL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.FISH_BARREL),
            new TriggerItem(ItemId.FISH_BARREL_OPEN),
            new TriggerItem(ItemId.FISH_SACK_BARREL),
            new TriggerItem(ItemId.FISH_SACK_BARREL_OPEN),
        };

        storage = storage.setMaximumTotalQuantity(28).storableItems(
            // Small net
            new StorableItem(ItemId.RAW_SHRIMPS).checkName("Shrimp"),
            new StorableItem(ItemId.RAW_ANCHOVIES).checkName("Anchovies"),
            new StorableItem(ItemId.RAW_MONKFISH).checkName("Monkfish"),

            // Big net
            new StorableItem(ItemId.RAW_MACKEREL).checkName("Mackerel"),
            new StorableItem(ItemId.RAW_COD).checkName("Cod"),
            new StorableItem(ItemId.RAW_BASS).checkName("Bass"),

            // Barbarian
            new StorableItem(ItemId.LEAPING_TROUT).checkName("Leaping trout"),
            new StorableItem(ItemId.LEAPING_SALMON).checkName("Leaping salmon"),
            new StorableItem(ItemId.LEAPING_STURGEON).checkName("Leaping sturgeon"),

            // Rod
            new StorableItem(ItemId.RAW_SARDINE).checkName("Sardine"),
            new StorableItem(ItemId.RAW_HERRING).checkName("Herring"),
            new StorableItem(ItemId.RAW_TROUT).checkName("Trout"),
            new StorableItem(ItemId.RAW_PIKE).checkName("Pike"),
            new StorableItem(ItemId.RAW_SLIMY_EEL).checkName("Slimy swamp eel", "Slimy eel"),
            new StorableItem(ItemId.RAW_SALMON).checkName("Salmon"),
            new StorableItem(ItemId.RAW_RAINBOW_FISH).checkName("Rainbow fish"),
            new StorableItem(ItemId.RAW_CAVE_EEL).checkName("Cave eel"),
            new StorableItem(ItemId.RAW_LAVA_EEL).checkName("Lava eel"),
            new StorableItem(ItemId.INFERNAL_EEL).checkName("Infernal eel"),
            new StorableItem(ItemId.RAW_ANGLERFISH).checkName("Anglerfish"),
            new StorableItem(ItemId.SACRED_EEL).checkName("Sacred eel"),

            // Harpoon
            new StorableItem(ItemId.RAW_TUNA).checkName("Tuna"),
            new StorableItem(ItemId.RAW_SWORDFISH).checkName("Swordfish"),
            new StorableItem(ItemId.RAW_SHARK).checkName("Shark"),

            // Aerial
            new StorableItem(ItemId.BLUEGILL).checkName("Bluegill"),
            new StorableItem(ItemId.COMMON_TENCH).checkName("Common tench"),
            new StorableItem(ItemId.MOTTLED_EEL).checkName("Mottled eel"),
            new StorableItem(ItemId.GREATER_SIREN).checkName("Greater siren"),

            // Cage
            new StorableItem(ItemId.RAW_LOBSTER).checkName("Lobster"),
            new StorableItem(ItemId.RAW_DARK_CRAB).checkName("Dark crab"),

            // Sailing
            new StorableItem(ItemId.RAW_SWORDTIP_SQUID).checkName("Swordtip squid"),
            new StorableItem(ItemId.RAW_JUMBO_SQUID).checkName("Jumbo squid"),
            new StorableItem(ItemId.RAW_GIANT_KRILL).checkName("Giant krill"),
            new StorableItem(ItemId.RAW_HADDOCK).checkName("Haddock"),
            new StorableItem(ItemId.RAW_YELLOWFIN).checkName("Yellowfin"),
            new StorableItem(ItemId.RAW_HALIBUT).checkName("Halibut"),
            new StorableItem(ItemId.RAW_BLUEFIN).checkName("Bluefin"),
            new StorableItem(ItemId.RAW_MARLIN).checkName("Marlin"),

            // Other
            new StorableItem(ItemId.RAW_KARAMBWAN).checkName("Karambwan"),
            new StorableItem(ItemId.RAW_SEA_TURTLE).checkName("Sea turtle"),
            new StorableItem(ItemId.RAW_MANTA_RAY).checkName("Manta ray")
        );

        this.triggers.addAll(List.of(
            // Check or empty already empty.
            new OnChatMessage("(Your|The) barrel is empty.").onItemClick().emptyStorage(),

            // Catch fish.
            new OnChatMessage("You catch (a|an|some) (?<fish>.+).?").matcherConsumer(m -> {
                lastCaughtFish = getStorageItemFromName(m.group("fish"), 1);
                storage.add(lastCaughtFish);
            }).requiredItem(ItemId.FISH_BARREL_OPEN, ItemId.FISH_SACK_BARREL_OPEN),

            // Extra fish.
            new OnChatMessage(".* enabled you to catch an extra fish.").requiredItem(ItemId.FISH_BARREL_OPEN, ItemId.FISH_SACK_BARREL_OPEN).consumer(() -> {
                storage.add(lastCaughtFish);
            }),

            // Replace "Empty" with proper "Empty to bank".
            new OnMenuEntryAdded("Empty").replaceOption(FredsItemChargesPlugin.menuOptionEmptyToBank).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Check.
            new OnChatMessage("The barrel contains:").stringConsumer(s -> {
                storage.clear();

                final Pattern pattern = Pattern.compile("(?<quantity>\\d+).x.(?<fish>.*?)(,|$)");
                final Matcher matcher = pattern.matcher(s);

                while (matcher.find()) {
                    storage.put(getStorageItemFromName(matcher.group("fish"), Integer.parseInt(matcher.group("quantity"))));
                }
            }).onItemClick(),

            // Fill from inventory.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onMenuOption("Fill"),

            // Use fish on barrel.
            new OnItemContainerChanged(INVENTORY).fillStorageFromInventory().onUseStorageItemOnChargedItem(storage.getStorableItems()),

            // Empty to deposit box.
            new OnChatMessage("You empty the barrel.").onMenuOption(FredsItemChargesPlugin.menuOptionEmptyToBank).emptyStorage(),

            // Hide destroy.
            new OnMenuEntryAdded("Destroy").hide()
        ));
    }
}
