package com.fred4106.improvedCharges.items.potions;

import com.fred4106.improvedCharges.item.triggers.TriggerItem;
import com.fred4106.improvedCharges.store.Provider;
import com.fred4106.improvedCharges.item.triggers.*;
import com.fred4106.improvedCharges.store.*;
import net.runelite.api.gameval.*;

public class P_AttackMix extends _Potion {
    public P_AttackMix(Provider provider) {
        super("attack_mix", new TriggerItem[]{
            new TriggerItem(ItemID.BRUTAL_1DOSE1ATTACK).fixedCharges(1),
            new TriggerItem(ItemID.BRUTAL_2DOSE1ATTACK).fixedCharges(2),
        }, provider);
    }
}
