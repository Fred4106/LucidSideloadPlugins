package com.fred4106.improvedCharges.items.weapons.blowpipes;

import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.enums.HitsplatTarget;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.enums.*;
import com.fred4106.improvedCharges.store.ids.*;

import java.util.*;

public class _Blowpipe extends ChargedItemWithStorage {
    public _Blowpipe(
        String configKey,
        int itemId,
        Provider provider,
        TriggerItem[] items,
        boolean supportsAdamantiteDarts,
        boolean supportsRuniteDarts,
        int attackAnimationId
    ) {
        super(configKey, itemId, provider);
        this.items = items;

        List<StorableItem> storableItems = new ArrayList<>();
        storableItems.add(new StorableItem(ItemID.BRONZE_DART).checkName("Bronze dart"));
        storableItems.add(new StorableItem(ItemID.IRON_DART).checkName("Iron dart"));
        storableItems.add(new StorableItem(ItemID.STEEL_DART).checkName("Steel dart"));
        storableItems.add(new StorableItem(ItemID.MITHRIL_DART).checkName("Mithril dart"));
        if (supportsAdamantiteDarts) {
            storableItems.add(
                new StorableItem(ItemID.ADAMANT_DART).checkName("Adamant dart")
            );
        }
        if (supportsRuniteDarts) {
            storableItems.add(
                new StorableItem(ItemID.RUNE_DART).checkName("Rune dart")
            );
        }
        storage.storableItems(
            storableItems.toArray(new StorableItem[0])
        );

        triggers.addAll(List.of(
            // Check
            new OnChatMessage("Darts: (?<type>.+) x (?<amount>.+).").matcherConsumer(m -> {
                Optional<StorageItem> darts = getStorageItemFromName(m.group("type"), FredsItemChargesPlugin.getNumberFromCommaString(m.group("amount")));
                storage.clearAndPut(darts);
            }).onItemClick(),

            // Attack
            new OnHitsplatApplied(HitsplatTarget.ENEMY, HitsplatGroup.ALL).isEquipped().hasAnimationId(attackAnimationId).consumer(() -> {
                for (StorageItem item : storage.getStorage().getItems()) {
                    if (FredsItemChargesPlugin.guessIfRangedAmmoRetrievalWasSuccessful(provider)) {
                        storage.remove(item.itemId, 1);
                    }
                }
            })
        ));
    }
}
