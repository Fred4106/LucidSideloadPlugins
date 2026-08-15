package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItemWithStorageEmptyable;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.OnXpDrop;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.WidgetId;
import net.runelite.api.*;
import net.runelite.api.gameval.ItemID;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.ids.*;

import java.util.*;

public class U_CoalBag extends ChargedItemWithStorageEmptyable {
    public U_CoalBag(Provider provider) {
        super(FredsItemChargesConfig.coal_bag, ItemID.COAL_BAG, provider);
        this.storage = storage
            .storableItems(new StorableItem(ItemID.COAL).checkName("Coal"))
            .setMaximumTotalQuantity(27)
            .setMaximumTotalQuantityWithEquippedItem(36, ItemID.SKILLCAPE_SMITHING, ItemID.SKILLCAPE_SMITHING_TRIMMED);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.COAL_BAG),
            new TriggerItem(ItemID.COAL_BAG_OPEN),
        };

        this.triggers.addAll(List.of(
            // Check or empty.
            new OnChatMessage("The coal bag is( now)? empty.").emptyStorage(),

            // Fill or check with 1 coal.
            new OnChatMessage("The coal bag( still)? contains one piece of coal.").consumer(() -> {
                storage.put(ItemID.COAL, 1);
            }),

            // Check or empty with not enough inventory space.
            new OnChatMessage("The coal bag( still)? contains (?<charges>.+) pieces of coal.").matcherConsumer((m) -> {
                storage.put(ItemID.COAL, Integer.parseInt(m.group("charges")));
            }),

            // Replace "Fill" with proper "Fill from bank".
            new OnMenuEntryAdded("Fill").replaceOption(FredsItemChargesPlugin.menuOptionFillFromBank).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Replace "Empty" with proper "Empty to bank".
            new OnMenuEntryAdded("Empty").replaceOption(FredsItemChargesPlugin.menuOptionEmptyToBank).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Mine coal with open bag.
            // Extra coal mined by celestial ring.
            // Extra coal mined by varrock platebody.
            new OnChatMessage(
                "(You manage to mine some coal.|Your Celestial ring allows you to mine an additional ore.|The Varrock platebody enabled you to mine an additional ore.)"
            ).onMenuOption("Mine").onMenuTarget("Coal rocks").requiredItem(ItemID.COAL_BAG_OPEN).addToStorage(ItemID.COAL, 1),

            // Superheat spells.
            new OnXpDrop(Skill.SMITHING).onMenuOption("Cast").onMenuTarget(
                "Superheat Item -> Lovakite ore",
                "Superheat Item -> Iron ore"
            ).consumer(() -> {
                storage.removeAndPrioritizeInventory(ItemID.COAL, 2);
            }),
            new OnXpDrop(Skill.SMITHING).onMenuOption("Cast").onMenuTarget(
                "Superheat Item -> Mithril ore"
            ).consumer(() -> {
                storage.removeAndPrioritizeInventory(ItemID.COAL, 4);
            }),
            new OnXpDrop(Skill.SMITHING).onMenuOption("Cast").onMenuTarget(
                "Superheat Item -> Adamantite ore"
            ).consumer(() -> {
                storage.removeAndPrioritizeInventory(ItemID.COAL, 6);
            }),
            new OnXpDrop(Skill.SMITHING).onMenuOption("Cast").onMenuTarget(
                "Superheat Item -> Runite ore"
            ).consumer(() -> {
                storage.removeAndPrioritizeInventory(ItemID.COAL, 8);
            }),

            // Hide destroy.
            new OnMenuEntryAdded("Destroy").hide()
        ));
    }
}
