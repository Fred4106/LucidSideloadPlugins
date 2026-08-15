package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuOptionClicked;
import com.fred4106.improvedCharges.item.triggers.OnVarbitsMapChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class U_PlankSack extends ChargedItemWithStorageEmptyable {
    public U_PlankSack(Provider provider) {
        super(FredsItemChargesConfig.plank_sack, ItemID.PLANK_SACK, provider);
        storage.setMaximumTotalQuantity(28).storableItems(
            new StorableItem(ItemID.WOODPLANK).checkName("Regular plank"),
            new StorableItem(ItemID.PLANK_OAK).checkName("Oak plank"),
            new StorableItem(ItemID.PLANK_TEAK).checkName("Teak plank"),
            new StorableItem(ItemID.PLANK_MAHOGANY).checkName("Mahogany plank"),
            new StorableItem(ItemID.PLANK_CAMPHOR).checkName("Camphor plank"),
            new StorableItem(ItemID.PLANK_IRONWOOD).checkName("Ironwood plank"),
            new StorableItem(ItemID.PLANK_ROSEWOOD).checkName("Rosewood plank")
        );

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.PLANK_SACK),
        };

        this.triggers.addAll(List.of(
            // Empty
            new OnChatMessage("Your sack is currently empty.").onItemClick().emptyStorage(),

            // Check
            new OnMenuOptionClicked("Check").consumer(() -> storage.clear()),
            new OnChatMessage("Regular planks: (?<charges>.+)").matcherConsumer(m -> storage.put(ItemID.WOODPLANK, Integer.parseInt(m.group("charges")))),
            new OnChatMessage("Oak planks: (?<charges>.+)").matcherConsumer(m -> storage.put(ItemID.PLANK_OAK, Integer.parseInt(m.group("charges")))),
            new OnChatMessage("Teak planks: (?<charges>.+)").matcherConsumer(m -> storage.put(ItemID.PLANK_TEAK, Integer.parseInt(m.group("charges")))),
            new OnChatMessage("Mahogany planks: (?<charges>.+)").matcherConsumer(m -> storage.put(ItemID.PLANK_MAHOGANY, Integer.parseInt(m.group("charges")))),
            new OnChatMessage("Camphor planks: (?<charges>.+)").matcherConsumer(m -> storage.put(ItemID.PLANK_CAMPHOR, Integer.parseInt(m.group("charges")))),
            new OnChatMessage("Ironwood planks: (?<charges>.+)").matcherConsumer(m -> storage.put(ItemID.PLANK_IRONWOOD, Integer.parseInt(m.group("charges")))),
            new OnChatMessage("Rosewood planks: (?<charges>.+)").matcherConsumer(m -> storage.put(ItemID.PLANK_ROSEWOOD, Integer.parseInt(m.group("charges")))),

            // Contents changed
            new OnVarbitsMapChanged(
                Map.of(
                    VarbitID.PLANK_SACK_PLAIN, ItemID.WOODPLANK,
                    VarbitID.PLANK_SACK_OAK, ItemID.PLANK_OAK,
                    VarbitID.PLANK_SACK_TEAK, ItemID.PLANK_TEAK,
                    VarbitID.PLANK_SACK_MAHOGANY, ItemID.PLANK_MAHOGANY,
                    VarbitID.PLANK_SACK_CAMPHOR, ItemID.PLANK_CAMPHOR,
                    VarbitID.PLANK_SACK_IRONWOOD, ItemID.PLANK_IRONWOOD,
                    VarbitID.PLANK_SACK_ROSEWOOD, ItemID.PLANK_ROSEWOOD
                )
            )
        ));
    }
}
