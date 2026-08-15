package com.fred4106.improvedCharges.items.utils;

import com.fred4106.improvedCharges.item.ChargedItem;
import com.fred4106.improvedCharges.item.triggers.OnVarbitChanged;
import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import net.runelite.api.gameval.*;
import com.fred4106.improvedCharges.*;
import com.fred4106.improvedCharges.item.*;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.Provider;

import java.util.*;

public class U_BowStringSpool extends ChargedItem {
    public U_BowStringSpool(Provider provider) {
        super(FredsItemChargesConfig.bow_string_spool, ItemID.BOWSTRING_SPOOL, provider);

        this.items = new TriggerItem[]{
            new TriggerItem(ItemID.BOWSTRING_SPOOL)
        };

        this.triggers.addAll(List.of(
            new OnVarbitChanged(VarbitID.BOWSTRING_SPOOL_CHARGES).setDynamically()
        ));
    }
}
