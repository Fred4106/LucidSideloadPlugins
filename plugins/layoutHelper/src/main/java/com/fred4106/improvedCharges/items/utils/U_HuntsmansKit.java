package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.FredsItemChargesPlugin;
import com.fred4106.improvedCharges.item.ChargedItemWithStorage;
import com.fred4106.improvedCharges.item.storage.StorableItem;
import com.fred4106.improvedCharges.item.triggers.OnItemContainerChanged;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ItemContainerId;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.store.ids.WidgetId;

import java.util.List;

public class U_HuntsmansKit extends ChargedItemWithStorage {
    public U_HuntsmansKit(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.HUNTSMANS_KIT, ItemId.HUNTSMANS_KIT, provider);
        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.HUNTSMANS_KIT)
        };

        this.storage = storage.storableItems(
            new StorableItem(ItemId.BIRD_SNARE),
            new StorableItem(ItemId.BUTTERFLY_NET),
            new StorableItem(ItemId.BUTTERFLY_JAR),
            new StorableItem(ItemId.RABBIT_SNARE),
            new StorableItem(ItemId.SMALL_FISHING_NET),
            new StorableItem(ItemId.MAGIC_BOX),
            new StorableItem(ItemId.TEASING_STICK),
            new StorableItem(ItemId.WOOD_CAMO_TOP),
            new StorableItem(ItemId.WOOD_CAMO_LEGS),
            new StorableItem(ItemId.JUNGLE_CAMO_TOP),
            new StorableItem(ItemId.JUNGLE_CAMO_LEGS),
            new StorableItem(ItemId.LARUPIA_HAT),
            new StorableItem(ItemId.LARUPIA_TOP),
            new StorableItem(ItemId.LARUPIA_LEGS),
            new StorableItem(ItemId.KYATT_HAT),
            new StorableItem(ItemId.KYATT_TOP),
            new StorableItem(ItemId.KYATT_LEGS),
            new StorableItem(ItemId.GUILD_HUNTER_HEADWEAR),
            new StorableItem(ItemId.GUILD_HUNTER_TOP),
            new StorableItem(ItemId.GUILD_HUNTER_LEGS),
            new StorableItem(ItemId.GUILD_HUNTER_BOOTS),
            new StorableItem(ItemId.RING_OF_PURSUIT),
            new StorableItem(ItemId.NOOSE_WAND),
            new StorableItem(ItemId.MAGIC_BUTTERFLY_NET),
            new StorableItem(ItemId.BOX_TRAP),
            new StorableItem(ItemId.UNLIT_TORCH),
            new StorableItem(ItemId.ROPE),
            new StorableItem(ItemId.HUNTERS_SPEAR),
            new StorableItem(ItemId.POLAR_CAMO_TOP),
            new StorableItem(ItemId.POLAR_CAMO_LEGS),
            new StorableItem(ItemId.DESERT_CAMO_TOP),
            new StorableItem(ItemId.DESERT_CAMO_LEGS),
            new StorableItem(ItemId.GRAAHK_HEADDRESS),
            new StorableItem(ItemId.GRAAHK_TOP),
            new StorableItem(ItemId.GRAAHK_LEGS),
            new StorableItem(ItemId.HUNTER_HOOD),
            new StorableItem(ItemId.HUNTER_CAPE),
            new StorableItem(ItemId.HUNTER_CAPE_TRIMMED),
            new StorableItem(ItemId.IMPLING_JAR)
        );

        this.triggers.addAll(List.of(
            // Fill from inventory.
            new OnItemContainerChanged(ItemContainerId.INVENTORY).fillStorageFromInventory().onMenuOption("Fill", FredsItemChargesPlugin.menuOptionFillFromInventory),

            // Empty to inventory.
            new OnItemContainerChanged(ItemContainerId.INVENTORY).emptyStorageToInventory().onMenuOption("Empty", FredsItemChargesPlugin.menuOptionEmptyToInventory),

            // Update from item container when viewing huntsmans kit contents.
            new OnItemContainerChanged(ItemContainerId.HUNTSMANS_KIT).updateStorage(),

            // Replace "Use" with proper Fill/Empty option.
            new OnMenuEntryAdded("Use").replaceOptionConsumer(() -> getMenuOptionForUse()).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),
            new OnMenuEntryAdded("Use").replaceOptionConsumer(() -> getMenuOptionForUse()).isWidgetVisible(WidgetId.BANK, WidgetId.DEPOSIT_BOX),

            // Hide destroy option.
            new OnMenuEntryAdded("Destroy").hide()
        ));
    }

    private String getMenuOptionForUse() {
        return storage.isStorableItemInInventory()
                ? FredsItemChargesPlugin.menuOptionFillFromInventory
                : FredsItemChargesPlugin.menuOptionEmptyToInventory;
    }
}
