package com.fred4106.improvedCharges.items.boots;

import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnGraphicChanged;
import com.fred4106.improvedCharges.item.triggers.OnResetDaily;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.ids.ChargeId;
import com.fred4106.improvedCharges.store.Provider;

public class B_FremennikSeaBoots extends ChargedItem {
    public B_FremennikSeaBoots(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.FREMENNIK_SEA_BOOTS, ItemId.FREMENNIK_SEA_BOOTS_1, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.FREMENNIK_SEA_BOOTS_1),
            new TriggerItem(ItemId.FREMENNIK_SEA_BOOTS_2),
            new TriggerItem(ItemId.FREMENNIK_SEA_BOOTS_3),
            new TriggerItem(ItemId.FREMENNIK_SEA_BOOTS_4).fixedCharges(ChargeId.UNLIMITED),
        };

        this.triggers = new TriggerBase[]{
            // Try to teleport while empty.
            new OnChatMessage("You have already used your available teleport for today. Try again tomorrow when the boots have recharged.").setFixedCharges(0),

            // Teleport.
            new OnGraphicChanged(111).onItemClick().decreaseCharges(1),

            // Daily reset.
            new OnResetDaily().specificItem(ItemId.FREMENNIK_SEA_BOOTS_1).setFixedCharges(1),
            new OnResetDaily().specificItem(ItemId.FREMENNIK_SEA_BOOTS_2).setFixedCharges(3), // Updated from 1 to 3 charges https://oldschool.runescape.wiki/w/Update:Poll_85_-_Bridges,_Boots,_Ropes_%26_Roots#Fremennik_Sea_Boots
            new OnResetDaily().specificItem(ItemId.FREMENNIK_SEA_BOOTS_3).setFixedCharges(5), // Updated from 1 to 5 charges https://oldschool.runescape.wiki/w/Update:Poll_85_-_Bridges,_Boots,_Ropes_%26_Roots#Fremennik_Sea_Boots
        };
    }
}