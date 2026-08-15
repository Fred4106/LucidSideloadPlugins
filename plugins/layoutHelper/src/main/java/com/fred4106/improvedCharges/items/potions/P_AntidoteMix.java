package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_AntidoteMix extends _Potion {
    public P_AntidoteMix(Provider provider) {
        super("antidote_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_ANTIDOTE_1).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_ANTIDOTE_2).fixedCharges(2),
        }, provider);
    }
}
