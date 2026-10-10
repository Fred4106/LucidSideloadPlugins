package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.storage.StorageItem;
import com.fred4106.improvedCharges.item.triggers.OnAnimationChanged;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnItemContainerChanged;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.Client;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.storage.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class U_QuetzalWhistle extends ChargedItem {
    public U_QuetzalWhistle(Provider provider) {
        super(FredsItemChargesConfig.quetzal_whistle, ItemID.HG_QUETZALWHISTLE_BASIC, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.HG_QUETZALWHISTLE_BASIC).maxCharges(5),
            new TriggerItem(ItemID.HG_QUETZALWHISTLE_ENHANCED).maxCharges(20),
            new TriggerItem(ItemID.HG_QUETZALWHISTLE_PERFECTED).maxCharges(50),
        };

        this.triggers.addAll(List.of(
            // Check.
            new OnChatMessage("Your quetzal whistle has (?<charges>.+) charges? remaining.").setDynamicallyCharges(),

            // Teleport.
            new OnAnimationChanged(AnimationID.HUMAN_QUETZAL_WHISTLE).decreaseCharges(1),

            // Teleport menu entry.
            new OnMenuEntryAdded("Signal").replaceOption("Teleport"),

            // Craft basic quetzal whistle.
            new OnChatMessage("You craft yourself a basic quetzal whistle.").setFixedCharges(0),

            // Fully charged.
            new OnChatMessage("Looks like the birds are all full for now. Make them work a bit before feeding them again!").requiredItem(ItemID.HG_QUETZALWHISTLE_BASIC).setFixedCharges(5),
            new OnChatMessage("Looks like the birds are all full for now. Make them work a bit before feeding them again!").requiredItem(ItemID.HG_QUETZALWHISTLE_ENHANCED).setFixedCharges(20),
            new OnChatMessage("Looks like the birds are all full for now. Make them work a bit before feeding them again!").requiredItem(ItemID.HG_QUETZALWHISTLE_PERFECTED).setFixedCharges(50),

            // Partially charged.
            new OnItemContainerChanged(InventoryID.INV).onMenuOption("Recharge-whistle").hasChatMessage("Soar Leader Pitri|There you go. Some whistle charges for you!").onInventoryDifference(itemsDifference -> {
                for (StorageItem item : itemsDifference.getItems()) {
                    switch (item.itemId) {
                        case ItemID.HG_SEEDSACK:
                        case ItemID.HUNTINGBEAST_WILD_MEAT:
                        case ItemID.HUNTINGBEAST_BARBED_MEAT:
                        case ItemID.HUNTING_LARUPIA_MEAT:
                            increaseCharges(Math.abs(item.getQuantity()));
                            break;
                        case ItemID.HUNTING_GRAAHK_MEAT:
                        case ItemID.HUNTING_KYATT_MEAT:
                        case ItemID.HUNTING_FENNECFOX_MEAT:
                            increaseCharges(Math.abs(item.getQuantity()) * 2);
                            break;
                        case ItemID.HUNTINGBEAST_SPEEDY2_MEAT:
                        case ItemID.HUNTING_ANTELOPESUN_MEAT:
                        case ItemID.HUNTING_ANTELOPEMOON_MEAT:
                            increaseCharges(Math.abs(item.getQuantity()) * 3);
                            break;
                    }
                }
            }),

            // Dynamic teleport menu option
            new OnMenuEntryAdded("Last-destination").replaceOptionConsumer(() -> {
                String last = getLastDestinationString(provider.client);
                if (!last.isBlank())
                    return "Last-destination (" + last + ")";
                return "Last-destination";
            })
        ));
    }

    private String getLastDestinationString(Client client) {
        List<Integer> quetzalRows = client.getDBRowsByValue(
            DBTableID.Quetzal.ID,
            DBTableID.Quetzal.COL_ID,
            0,
            client.getVarbitValue(VarbitID.QUETZAL_LAST_DESTINATION));
        String last = "";
        if (!quetzalRows.isEmpty())
        {
            last = (String) client.getDBTableField(quetzalRows.get(0), DBTableID.Quetzal.COL_NAME, 0)[0];
        }

        return last;
    }
}
