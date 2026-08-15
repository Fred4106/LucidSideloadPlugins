package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnXpDrop;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import net.runelite.api.*;
import net.runelite.api.gameval.ItemID;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.*;

import static com.fred4106.improvedCharges.FredsItemChargesPlugin.*;

public class U_BottomlessCompostBucket extends ChargedItemWithStorage {
    public U_BottomlessCompostBucket(Provider provider) {
        super(FredsItemChargesConfig.bottomless_compost_bucket, ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED, provider);
        storage = storage.setMaximumTotalQuantity(10_000).storableItems(
            new StorableItem(ItemID.BUCKET_ULTRACOMPOST).checkName("ultra"),
            new StorableItem(ItemID.BUCKET_SUPERCOMPOST).checkName("super"),
            new StorableItem(ItemID.BUCKET_COMPOST).checkName("regular").displayName("Regular compost")
        );

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BOTTOMLESS_COMPOST_BUCKET).fixedCharges(0),
            new TriggerItem(ItemID.BOTTOMLESS_COMPOST_BUCKET_FILLED),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your bottomless compost bucket is currently holding one use of (?<type>.+) ?compost.").matcherConsumer(m -> {
                storage.clearAndPut(getStorageItemFromName(m.group("type"), 1));
            }),
            new OnChatMessage("Your bottomless compost bucket is currently holding (?<quantity>.+) uses of (?<type>.+) ?compost.").matcherConsumer(m -> {
                int quantity = getNumberFromCommaString(m.group("quantity"));
                storage.clearAndPut(getStorageItemFromName(m.group("type"), quantity));
            }),

            // Use compost on a patch, run on next gametick, because the "You treat" message appears on same tick after this one.
            new OnChatMessage("Your bottomless compost bucket has a single use of (?<type>.+) ?compost remaining.").matcherConsumer(m -> {
                provider.store.addConsumerToNextTickQueue(() -> {
                    storage.clearAndPut(getStorageItemFromName(m.group("type"), 1));
                });
            }),
            new OnChatMessage("Your bottomless compost bucket has (?<quantity>.+) uses of (?<type>.+) ?compost remaining.").matcherConsumer(m -> {
                provider.store.addConsumerToNextTickQueue(() -> {
                    int quantity = getNumberFromCommaString(m.group("quantity"));
                    storage.clearAndPut(getStorageItemFromName(m.group("type"), quantity));
                });
            }),
            new OnChatMessage("You treat the .* with (?<type>.*) ?compost.").matcherConsumer(m -> {
                String type = m.group("type");
                storage.remove(getStorageItemFromName(type.isEmpty() ? "regular" : type, 1));
            }).onItemClick(),

            // Discard.
            new OnChatMessage("You discard the contents of your bottomless compost bucket.").emptyStorage(),

            // Empty.
            new OnChatMessage("Your bottomless compost bucket has run out of compost!").emptyStorage(),

            // Fill.
            new OnChatMessage("You fill your bottomless compost bucket with a single bucket of (?<type>.+) ?compost. Your bottomless compost bucket now contains a total of (?<quantity>.+) uses.").matcherConsumer(m -> {
                int quantity = getNumberFromCommaString(m.group("quantity"));
                storage.clearAndPut(getStorageItemFromName(m.group("type"), quantity));
            }),
            new OnChatMessage("You fill your bottomless compost bucket with .* buckets of (?<type>.+) ?compost. Your bottomless compost bucket now contains a total of (?<quantity>.+) uses.").matcherConsumer(m -> {
                int quantity = getNumberFromCommaString(m.group("quantity"));
                storage.clearAndPut(getStorageItemFromName(m.group("type"), quantity));
            }),

            // Almost full.
            new OnChatMessage("Your bottomless compost bucket is just about full. You won't be able to squeeze any more compost in there.").consumer(() -> {
                if (getCompostType().isPresent()) {
                    storage.clearAndPut(getCompostType().get().itemId, 9999);
                }
            }),

            // Full.
            new OnChatMessage("Your bottomless compost bucket is now full!").consumer(() -> {
                if (getCompostType().isPresent()) {
                    storage.clearAndPut(getCompostType().get().itemId, 10_000);
                }
            }),

            // Fill compost from bin.
            new OnXpDrop(Skill.FARMING).unallowedItem(ItemID.BUCKET_EMPTY).onMenuOption("Take").onMenuTarget("Compost Bin", "Big Compost Bin").consumer(() -> {
                if (getCompostType().isPresent()) {
                    storage.add(getCompostType().get().itemId, 2);
                }
            }),

            // Use on compost bin.
            new OnXpDrop(Skill.FARMING).onMenuOption("Use").onMenuTarget("Bottomless compost bucket -> Compost Bin", "Bottomless compost bucket -> Big Compost Bin").consumer(() -> {
                if (getCompostType().isPresent()) {
                    storage.add(getCompostType().get().itemId, 2);
                }
            })
        ));
    }

    private Optional<StorageItem> getCompostType() {
        for (StorageItem storageItem : getStorage().getItems()) {
            if (storageItem.getQuantity() > 0) {
                return Optional.of(storageItem);
            }
        }

        return Optional.empty();
    }
}
