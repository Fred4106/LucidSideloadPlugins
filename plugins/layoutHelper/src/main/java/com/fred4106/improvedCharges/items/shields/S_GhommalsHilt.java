package com.fred4106.improvedCharges.items.shields;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnVarbitChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.*;

public class S_GhommalsHilt extends ChargedItem {
    public S_GhommalsHilt(Provider provider) {
        super(FredsItemChargesConfig.ghommals_hilt, ItemID.CA_OFFHAND_EASY, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.CA_OFFHAND_EASY).maxCharges(3),
            new TriggerItem(ItemID.CA_OFFHAND_MEDIUM).maxCharges(5),
            new TriggerItem(ItemID.CA_OFFHAND_HARD).unlimitedCharges(),
            new TriggerItem(ItemID.CA_OFFHAND_ELITE).maxCharges(3),
            new TriggerItem(ItemID.CA_OFFHAND_MASTER).maxCharges(5),
            new TriggerItem(ItemID.CA_OFFHAND_GRANDMASTER).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            new OnVarbitChanged(VarbitID.CA_TELEPORT_COUNT_TROLLHEIM).varbitValueConsumer(varbitValue -> {
                switch (itemId) {
                    case ItemID.CA_OFFHAND_EASY:
                        setCharges(3 - varbitValue);
                        break;
                    case ItemID.CA_OFFHAND_MEDIUM:
                        setCharges(5 - varbitValue);
                        break;
                }
            }),
            new OnVarbitChanged(VarbitID.CA_TELEPORT_COUNT_MORULREK).varbitValueConsumer(varbitValue -> {
                switch (itemId) {
                    case ItemID.CA_OFFHAND_ELITE:
                        setCharges(3 - varbitValue);
                        break;
                    case ItemID.CA_OFFHAND_MASTER:
                        setCharges(5 - varbitValue);
                        break;
                }
            })
        ));
    }
}
