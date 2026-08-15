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

public class A_CrystalHelm extends ChargedItem {
    public A_CrystalHelm(Provider provider) {
        super(FredsItemChargesConfig.crystal_helm, ItemID.CRYSTAL_HELMET, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.CRYSTAL_HELMET),
            new TriggerItem(ItemID.CRYSTAL_HELMET_HEFIN),
            new TriggerItem(ItemID.CRYSTAL_HELMET_ITHELL),
            new TriggerItem(ItemID.CRYSTAL_HELMET_IORWERTH),
            new TriggerItem(ItemID.CRYSTAL_HELMET_TRAHAEARN),
            new TriggerItem(ItemID.CRYSTAL_HELMET_CADARN),
            new TriggerItem(ItemID.CRYSTAL_HELMET_CRWYS),
            new TriggerItem(ItemID.CRYSTAL_HELMET_AMLODD),
            new TriggerItem(ItemID.CRYSTAL_HELMET_DEADMAN),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE_HEFIN).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE_ITHELL).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE_IORWERTH).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE_TRAHAEARN).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE_CADARN).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE_CRWYS).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE_AMLODD).fixedCharges(0),
            new TriggerItem(ItemID.CRYSTAL_HELMET_INACTIVE_DEADMAN).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("Your crystal helm has (?<charges>.+) charges? remaining").setDynamicallyCharges().onItemClick(),
            new OnHitsplatApplied(SELF, HitsplatGroup.SUCCESSFUL).isEquipped().decreaseCharges(1),

            // Auto-charge.
            new OnAutoChargeMessage("Crystal helm", "Crystal shard", 100, this)
        ));
    }
}
