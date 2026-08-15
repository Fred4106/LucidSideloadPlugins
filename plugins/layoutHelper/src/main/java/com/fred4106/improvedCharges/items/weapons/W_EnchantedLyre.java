package com.fred4106.improvedCharges.items.weapons;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnMenuEntryAdded;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;

import java.util.*;

public class W_EnchantedLyre extends ChargedItem {
    public W_EnchantedLyre(Provider provider) {
        super(FredsItemChargesConfig.enchanted_lyre, ItemID.VIKING_ENCHANTED_STRUNG_LYRE, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.VIKING_ENCHANTED_STRUNG_LYRE).fixedCharges(0),
            new TriggerItem(ItemID.MAGIC_STRUNG_LYRE).fixedCharges(1),
            new TriggerItem(ItemID.MAGIC_STRUNG_LYRE_2).fixedCharges(2),
            new TriggerItem(ItemID.MAGIC_STRUNG_LYRE_3).fixedCharges(3),
            new TriggerItem(ItemID.MAGIC_STRUNG_LYRE_4).fixedCharges(4),
            new TriggerItem(ItemID.MAGIC_STRUNG_LYRE_5).fixedCharges(5),
            new TriggerItem(ItemID.MAGIC_STRUNG_LYRE_INFINITE).unlimitedCharges(),
        };

        this.triggers.addAll(List.of(
            new OnMenuEntryAdded("Play").replaceOption("Teleport")
        ));
    }
}
