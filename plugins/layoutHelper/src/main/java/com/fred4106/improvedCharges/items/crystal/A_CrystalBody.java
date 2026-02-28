package com.fred4106.improvedCharges.items.crystal;

import com.fred4106.improvedCharges.store.enums.HitsplatGroup;
import com.fred4106.improvedCharges.store.ids.ItemId;
import com.fred4106.improvedCharges.Constants;
import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnChatMessage;
import com.fred4106.improvedCharges.item.triggers.OnHitsplatApplied;
import com.fred4106.improvedCharges.item.triggers.TriggerBase;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;

import java.util.List;

import static com.fred4106.improvedCharges.store.enums.HitsplatTarget.SELF;

public class A_CrystalBody extends ChargedItem {
    public A_CrystalBody(final Provider provider) {
        super(Constants.CRYSTAL_BODY, ItemId.CRYSTAL_BODY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CRYSTAL_BODY),
            new TriggerItem(ItemId.CRYSTAL_BODY_HEFIN),
            new TriggerItem(ItemId.CRYSTAL_BODY_ITHELL),
            new TriggerItem(ItemId.CRYSTAL_BODY_IORWERTH),
            new TriggerItem(ItemId.CRYSTAL_BODY_TRAHAEARN),
            new TriggerItem(ItemId.CRYSTAL_BODY_CADARN),
            new TriggerItem(ItemId.CRYSTAL_BODY_CRWYS),
            new TriggerItem(ItemId.CRYSTAL_BODY_AMLODD),
            new TriggerItem(ItemId.CRYSTAL_BODY_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_BODY_HEFIN_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_BODY_ITHELL_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_BODY_IORWERTH_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_BODY_TRAHAEARN_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_BODY_CADARN_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_BODY_CRWYS_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_BODY_AMLODD_INACTIVE).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("Your crystal body has (?<charges>.+) charges? remaining").setDynamicallyCharges().onItemClick(),
            new OnHitsplatApplied(SELF, HitsplatGroup.SUCCESSFUL).isEquipped().decreaseCharges(1)
        ));
    }
}
