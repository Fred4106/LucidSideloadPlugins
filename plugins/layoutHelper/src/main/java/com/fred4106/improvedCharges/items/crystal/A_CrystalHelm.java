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

public class A_CrystalHelm extends ChargedItem {
    public A_CrystalHelm(final Provider provider) {
        super(com.fred4106.improvedCharges.Constants.CRYSTAL_HELM, ItemId.CRYSTAL_HELM, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemId.CRYSTAL_HELM),
            new TriggerItem(ItemId.CRYSTAL_HELM_HEFIN),
            new TriggerItem(ItemId.CRYSTAL_HELM_ITHELL),
            new TriggerItem(ItemId.CRYSTAL_HELM_IORWERTH),
            new TriggerItem(ItemId.CRYSTAL_HELM_TRAHAEARN),
            new TriggerItem(ItemId.CRYSTAL_HELM_CADARN),
            new TriggerItem(ItemId.CRYSTAL_HELM_CRWYS),
            new TriggerItem(ItemId.CRYSTAL_HELM_AMLODD),
            new TriggerItem(ItemId.CRYSTAL_HELM_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_HELM_HEFIN_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_HELM_ITHELL_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_HELM_IORWERTH_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_HELM_TRAHAEARN_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_HELM_CADARN_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_HELM_CRWYS_INACTIVE).fixedCharges(0),
            new TriggerItem(ItemId.CRYSTAL_HELM_AMLODD_INACTIVE).fixedCharges(0),
        };

        this.triggers.addAll(List.of(
            new OnChatMessage("Your crystal helm has (?<charges>.+) charges? remaining").setDynamicallyCharges().onItemClick(),
            new OnHitsplatApplied(SELF, HitsplatGroup.SUCCESSFUL).isEquipped().decreaseCharges(1)
        ));
    }
}
