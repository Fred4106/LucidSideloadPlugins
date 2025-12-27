package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnItemContainerChanged;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.AnimationId;
import com.fred4106.improvedCharges.store.ids.ItemContainerId;
import com.fred4106.improvedCharges.store.ids.ItemId;

public class U_QuetzalWhistle extends ChargedItem {
    public U_QuetzalWhistle(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.QUETZAL_WHISTLE, ItemId.QUETZAL_WHISTLE_BASIC, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.QUETZAL_WHISTLE_BASIC).maxCharges(5),
            new TriggerItem(ItemId.QUETZAL_WHISTLE_ENHANCED).maxCharges(20),
            new TriggerItem(ItemId.QUETZAL_WHISTLE_PERFECTED).maxCharges(50),
        };

        this.triggers = new TriggerBase[] {
            // Check.
            new OnChatMessage("Your quetzal whistle has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Teleport.
            new OnAnimationChanged(AnimationId.QUETZAL_WHISTLE_BIRD).decreaseCharges(1),

            // Teleport menu entry.
            new OnMenuEntryAdded("Signal").replaceOption("Teleport"),

            // Craft basic quetzal whistle.
            new OnChatMessage("You craft yourself a basic quetzal whistle.").setFixedCharges(0),

            // Fully charged.
            new OnChatMessage("Looks like the birds are all full for now. Make them work a bit before feeding them again!").requiredItem(ItemId.QUETZAL_WHISTLE_BASIC).setFixedCharges(5),
            new OnChatMessage("Looks like the birds are all full for now. Make them work a bit before feeding them again!").requiredItem(ItemId.QUETZAL_WHISTLE_ENHANCED).setFixedCharges(20),
            new OnChatMessage("Looks like the birds are all full for now. Make them work a bit before feeding them again!").requiredItem(ItemId.QUETZAL_WHISTLE_PERFECTED).setFixedCharges(50),

            // Partially charged.
            new OnItemContainerChanged(ItemContainerId.INVENTORY).onMenuOption("Recharge-whistle").hasChatMessage("Soar Leader Pitri|There you go. Some whistle charges for you!").onInventoryDifference(itemsDifference -> {
                for (final StorageItem item : itemsDifference.getItems()) {
                    switch (item.getId()) {
                        case ItemId.QUETZAL_FEED:
                        case ItemId.RAW_WILD_KEBBIT:
                        case ItemId.RAW_BARBTAILED_KEBBIT:
                        case ItemId.RAW_LARUPIA:
                            increaseCharges(Math.abs(item.getQuantity()));
                            break;
                        case ItemId.RAW_GRAAHK:
                        case ItemId.RAW_KYATT:
                        case ItemId.RAW_PYRE_FOX:
                            increaseCharges(Math.abs(item.getQuantity()) * 2);
                            break;
                        case ItemId.RAW_DASHING_KEBBIT:
                        case ItemId.RAW_SUNLIGHT_ANTELOPE:
                        case ItemId.RAW_MOONLIGHT_ANTELOPE:
                            increaseCharges(Math.abs(item.getQuantity()) * 3);
                            break;
                    }
                }
            })
        };
    }
}
