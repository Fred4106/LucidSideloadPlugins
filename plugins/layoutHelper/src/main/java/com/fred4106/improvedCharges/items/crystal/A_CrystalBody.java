package com.fred4106.improvedCharges.items.crystal;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnAutoChargeMessage;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import com.fred4106.improvedCharges.store.enums.*;

import java.util.*;

import static com.fred4106.improvedCharges.store.enums.HitsplatTarget.*;

public class A_CrystalBody extends ChargedItem {
    public A_CrystalBody(Provider provider) {
        super(FredsItemChargesConfig.crystal_body, ItemID.CRYSTAL_CHESTPLATE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_HEFIN),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_ITHELL),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_IORWERTH),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_TRAHAEARN),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_CADARN),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_CRWYS),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_AMLODD),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_DEADMAN),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE_HEFIN).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE_ITHELL).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE_IORWERTH).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE_TRAHAEARN).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE_CADARN).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE_CRWYS).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE_AMLODD).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_CHESTPLATE_INACTIVE_DEADMAN).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("Your crystal body has (?<charges>.+) charges? remaining").setDynamicallyCharges().onItemClick(),
            new OnHitsplatApplied(SELF, HitsplatGroup.SUCCESSFUL).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnAutoChargeMessage("Crystal body", "Crystal shard", 100, this)
        ));
    }
}
