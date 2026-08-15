package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_MagicEssenceMix extends _Potion {
    public P_MagicEssenceMix(Provider provider) {
        super("magic_essence_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSEMAGICESS).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSEMAGICESS).fixedCharges(2),
        }, provider);
    }
}
